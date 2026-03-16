package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.ConfirmAllocationRequest;
import org.luun.kitchencontrolbev1.dto.request.OrderDetailRequest;
import org.luun.kitchencontrolbev1.dto.request.OrderRequest;
import org.luun.kitchencontrolbev1.dto.response.FefoSuggestionResponse;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailFillResponse;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import org.luun.kitchencontrolbev1.dto.response.*;
import org.luun.kitchencontrolbev1.entity.*;
import org.luun.kitchencontrolbev1.enums.DeliveryStatus;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.repository.*;
import org.luun.kitchencontrolbev1.service.*;
import org.luun.kitchencontrolbev1.service.statustransitionhandler.OrderStatusTransitionHandler;
import org.luun.kitchencontrolbev1.service.statusvalidator.OrderStatusValidator;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserService userService;
    private final ProductService productService;
    private final StoreService storeService;
    private final InventoryRepository inventoryRepository;
    private final LogBatchRepository logBatchRepository;
    private final OrderDetailFillRepository orderDetailFillRepository;

    private final OrderRepository orderRepository;
    private final OrderStatusValidator orderStatusValidator;
    private final OrderStatusTransitionHandler orderStatusTransitionHandler;

    @Override
    public List<OrderResponse> getOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    @Override
    public List<OrderResponse> getOrdersByStoreId(Integer storeId) {
        List<Order> orders = orderRepository.findByStore_StoreId(storeId);
        if (orders == null) {
            throw new RuntimeException("Orders not found with store id: " + storeId);
        }
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Creating orders method
    @Override
    @Transactional
    public void createOrder(OrderRequest request) {
        // 1. Find Store
        Store store = storeService.getStoreById(request.getStoreId());

        // 2. Create Order object
        Order order = new Order();
        order.setStore(store);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.WAITING);
        order.setComment(request.getComment());

        // Set type and parent order for order
        if (request.getType() == "SUPPLEMENT") {
            order.setType("SUPPLEMENT");
            order.setParent_order_id(request.getParentOrderId());
        } else {
            order.setType("NORMAL");
        }

        // 3. Set the parent Order for the detail
        if (request.getOrderDetails() != null) {
            for (OrderDetailRequest detailRequest : request.getOrderDetails()) {

                Product product = productService.getProductById(detailRequest.getProductId());

                order.addDetail(product, detailRequest.getQuantity());
            }
        }

        // 4. Save the Order (and thanks to Cascade, OrderDetails will be saved too)
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Integer id, OrderStatus newStatus, String note) {
        Order order = getOrderById(id);

        // 1 validate transition
        orderStatusValidator.validate(order.getStatus(), newStatus);

        // 2 run business logic
        orderStatusTransitionHandler.handle(order, newStatus);

        // 3 update status
        order.setStatus(newStatus);

        // 4 Add a note for the order if available
        String currentNote = order.getComment();
        if (note != null && !note.isBlank()) {
            if (currentNote != null && !currentNote.isBlank()) {
                order.setComment(currentNote + " | Reject reason:" + note);
            } else {
                order.setComment("Reject reason:" + note);
            }
        }

        Delivery delivery = order.getDelivery();
        if (delivery != null) {
            checkDeliveryCompletion(delivery);
        }
    }

    private void checkDeliveryCompletion(Delivery delivery) {

        boolean allFinished = delivery.getOrders().stream()
                .allMatch(o -> o.getStatus() == OrderStatus.DONE ||
                        o.getStatus() == OrderStatus.DAMAGED ||
                        o.getStatus() == OrderStatus.PARTIAL_DELIVERED);

        if (allFinished) {
            delivery.setStatus(DeliveryStatus.DONE);
        }
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus orderStatus) {
        List<Order> orders = orderRepository.findByStatus(orderStatus);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByShipperId(Integer shipperId) {

        User user = userService.getUserById(shipperId);

        if (!user.getRole().getRoleName().equals("SHIPPER")) {
            throw new RuntimeException("User is not a shipper");
        }

        List<Order> orders = orderRepository.findByDelivery_Shipper_UserId(shipperId);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FefoSuggestionResponse getFefoAllocationSuggestion(Integer orderId) {
        // 1. Lấy thông tin đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra trạng thái đơn hàng đó có đang là WAITING hay ko.
        if (order.getStatus() != OrderStatus.WAITING) {
            throw new RuntimeException("Chỉ có thể tạo đề xuất cho đơn hàng ở trạng thái WAITING.");
        }

        // Tạo ra các object khởi tạo để chuẩn bị cho việc gán giá trị vào
        List<FefoSuggestionResponse.ProductAllocationSuggestion> productSuggestions = new ArrayList<>();
        boolean isFullyFulfillable = true;
        StringBuilder messageBuilder = new StringBuilder();

        // 2. Lặp qua từng sản phẩm trong đơn hàng để chạy FEFO
        for (OrderDetail detail : order.getOrderDetails()) {
            // Lấy số lượng của từng loại sản phẩm mà store cần đặt
            float requiredQuantity = detail.getQuantity();

            // 3. Query FEFO: Lấy các lô hàng tồn kho, sắp xếp theo ngày hết hạn gần nhất
            List<Inventory> availableBatches = inventoryRepository
                    .findValidInventoriesForProductOrderByExpiryDateAsc(detail.getProduct().getProductId());

            // Tạo ra các object khởi tạo để chuẩn bị cho việc gán giá trị vào
            List<FefoSuggestionResponse.ProductAllocationSuggestion.BatchSuggestion> batchSuggestions = new ArrayList<>();
            float allocatedQuantity = 0;

            // 4. Phân bổ số lượng từ các lô đã sắp xếp
            for (Inventory inventory : availableBatches) {
                // Kiểm tra xem đã phân bổ đủ số lượng cho sản phẩm này chưa
                if (allocatedQuantity >= requiredQuantity) {
                    break; // Đã đủ số lượng cho sản phẩm này
                }

                //
                float quantityToPick = Math.min(requiredQuantity - allocatedQuantity, inventory.getQuantity());

                batchSuggestions.add(FefoSuggestionResponse.ProductAllocationSuggestion.BatchSuggestion.builder()
                        .batchId(inventory.getBatch().getBatchId())
                        .expiryDate(inventory.getExpiryDate())
                        .availableQuantityInBatch(inventory.getQuantity())
                        .suggestedQuantityToPick(quantityToPick)
                        .build());

                allocatedQuantity += quantityToPick;
            }

            // 5. Xây dựng response cho sản phẩm này
            productSuggestions.add(FefoSuggestionResponse.ProductAllocationSuggestion.builder()
                    .orderDetailId(detail.getOrderDetailId())
                    .productName(detail.getProduct().getProductName())
                    .requiredQuantity(requiredQuantity)
                    .batchSuggestions(batchSuggestions)
                    .build());

            // 6. Kiểm tra xem sản phẩm này có được đáp ứng đủ không
            // Nếu ko thì sẽ tạo ra mesage để gắn vào DTO gốc là FefoSuggestionResponse
            if (allocatedQuantity < requiredQuantity) {
                isFullyFulfillable = false;
                messageBuilder.append(String.format("Không đủ hàng cho sản phẩm '%s'. Cần %s, chỉ có thể cung cấp %s. ",
                        detail.getProduct().getProductName(), requiredQuantity, allocatedQuantity));
            }
        }

        // Kiểm tra xem tất cả sản phẩm đều có đủ hàng tồn kho chưa
        if (isFullyFulfillable) {
            messageBuilder.append("Tất cả sản phẩm đều có đủ hàng tồn kho.");
        }

        // 7. Trả về kết quả đề xuất cuối cùng
        return FefoSuggestionResponse.builder()
                .orderId(order.getOrderId())
                .storeName(order.getStore().getStoreName())
                .productSuggestions(productSuggestions)
                .isFulfillable(isFullyFulfillable)
                .message(messageBuilder.toString().trim())
                .build();
    }

    @Override
    @Transactional // Đảm bảo tất cả đều đc lưu thành công
    public void confirmAllocation(Integer orderId, ConfirmAllocationRequest request) {
        // 1. Lấy đơn hàng và xác thực trạng thái
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra xem đơn hàng có status đang ở WAITING ko ?
        if (order.getStatus() != OrderStatus.WAITING) {
            throw new RuntimeException("Chỉ có thể xác nhận cho đơn hàng ở trạng thái WAITING.");
        }

        // Chuyển danh sách order details gốc thành Map để tra cứu nhanh
        Map<Integer, OrderDetail> originalDetailsMap = order.getOrderDetails().stream()
                .collect(Collectors.toMap(OrderDetail::getOrderDetailId, Function.identity()));

        // 2. Giai đoạn VALIDATION: Kiểm tra toàn bộ request trước khi thực hiện bất kỳ
        // thay đổi nào
        for (ConfirmAllocationRequest.FinalProductAllocation finalAllocation : request.getFinalAllocations()) {
            // Lấy orderDetailId trong request để tra cứu Map của orderDetail gốc xem có hợp
            // lệ ko ?
            OrderDetail originalDetail = originalDetailsMap.get(finalAllocation.getOrderDetailId());
            if (originalDetail == null) {
                throw new RuntimeException(
                        "Yêu cầu chứa orderDetailId không hợp lệ: " + finalAllocation.getOrderDetailId());
            }

            // Kiểm tra tổng số lượng phân bổ mà warehouse tự động xếp có khớp với yêu cầu
            // không
            float totalAllocated = (float) finalAllocation.getBatchPicks()
                    .stream()
                    .mapToDouble(ConfirmAllocationRequest.FinalProductAllocation.FinalBatchPick::getQuantity)
                    .sum();

            if (Math.abs(totalAllocated - originalDetail.getQuantity()) > 0.00001) { // Dùng sai số nhỏ để so sánh số
                                                                                     // thực
                throw new RuntimeException(
                        String.format("Tổng số lượng phân bổ cho sản phẩm '%s' (%s) không khớp với yêu cầu (%s).",
                                originalDetail.getProduct().getProductName(),
                                totalAllocated, originalDetail.getQuantity()));
            }

            // Kiểm tra tồn kho thực tế tại thời điểm xác nhận (tránh race condition)
            for (ConfirmAllocationRequest.FinalProductAllocation.FinalBatchPick pick : finalAllocation
                    .getBatchPicks()) {
                // Kiểm tra xem warehouse chọn lô nào mà lô đó có tồn tại trong kho không
                Inventory inventory = inventoryRepository.findByBatchBatchId(pick.getBatchId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho cho lô: " + pick.getBatchId()));

                if (inventory.getQuantity() < pick.getQuantity()) {
                    throw new RuntimeException(
                            String.format("Tồn kho của lô %s cho sản phẩm '%s' không đủ. Yêu cầu %s, chỉ còn %s.",
                                    pick.getBatchId(), originalDetail.getProduct().getProductName(), pick.getQuantity(),
                                    inventory.getQuantity()));
                }
            }
        }

        // 3. Giai đoạn EXECUTION: Nếu tất cả validation đã qua, tiến hành lưu vào DB
        for (ConfirmAllocationRequest.FinalProductAllocation finalAllocation : request.getFinalAllocations()) {
            // Trong request, lấy từng orderDetailId trong class FinalAllocation ra để tiến
            // hành chuẩn bị lưu xuống DB
            OrderDetail originalDetail = originalDetailsMap.get(finalAllocation.getOrderDetailId());

            for (ConfirmAllocationRequest.FinalProductAllocation.FinalBatchPick pick : finalAllocation
                    .getBatchPicks()) {
                OrderDetailFill fill = new OrderDetailFill();
                fill.setOrderDetail(originalDetail);

                // Lấy từng lô ra gán vào fill để lưu xuống DB
                LogBatch batch = logBatchRepository.findById(pick.getBatchId())
                        .orElseThrow(() -> new RuntimeException("Lô không tồn tại: " + pick.getBatchId()));
                fill.setBatch(batch);
                fill.setQuantity(pick.getQuantity());
                fill.setCreatedAt(LocalDateTime.now());

                // Giả sử bạn có orderDetailFillRepository được inject
                orderDetailFillRepository.save(fill);
            }
        }

        // 4. Cập nhật trạng thái đơn hàng
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());

        // Delivery info
        if (order.getDelivery() != null) {
            response.setDeliveryId(order.getDelivery().getDeliveryId());
        }

        // Store info
        if (order.getStore() != null) {
            response.setStoreId(order.getStore().getStoreId());
            response.setStoreName(order.getStore().getStoreName());
        }

        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setImg(order.getImg());
        response.setComment(order.getComment());

        // Map details
        if (order.getOrderDetails() != null) {
            List<OrderDetailResponse> details = order.getOrderDetails().stream()
                    .map(this::mapToDetailResponse)
                    .collect(Collectors.toList());
            response.setOrderDetails(details);
        }

        // Map feedback
        if (order.getQualityFeedback() != null) {
            response.setFeedbackId(order.getQualityFeedback().getFeedbackId());
            response.setFeedbackRating(order.getQualityFeedback().getRating());
            response.setFeedbackComment(order.getQualityFeedback().getComment());
        }

        return response;
    }

    private OrderDetailResponse mapToDetailResponse(OrderDetail detail) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderDetailId(detail.getOrderDetailId());

        // Map Product info
        if (detail.getProduct() != null) {
            response.setProductId(detail.getProduct().getProductId());
            response.setProductName(detail.getProduct().getProductName());
        }

        // Map OrderDetailFill info
        if (detail.getOrderDetailFills() != null) {
            List<OrderDetailFillResponse> fills = detail.getOrderDetailFills().stream()
                    .map(this::mapToFillResponse)
                    .collect(Collectors.toList());
            response.setOrderDetailFills(fills);
        }

        response.setQuantity(detail.getQuantity());
        return response;
    }

    private OrderDetailFillResponse mapToFillResponse(OrderDetailFill fill) {

        OrderDetailFillResponse response = new OrderDetailFillResponse();

        response.setFillId(fill.getFillId());
        response.setOrderDetailId(fill.getOrderDetail().getOrderDetailId());
        response.setBatchId(fill.getBatch().getBatchId());
        response.setQuantity(fill.getQuantity());
        response.setCreatedAt(fill.getCreatedAt());

        return response;
    }
}
