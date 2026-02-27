package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.DeliveryResponse;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import org.luun.kitchencontrolbev1.entity.Delivery;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.luun.kitchencontrolbev1.entity.User;
import org.luun.kitchencontrolbev1.repository.DeliveryRepository;
import org.luun.kitchencontrolbev1.repository.UserRepository;
import org.luun.kitchencontrolbev1.repository.OrderRepository;
import org.luun.kitchencontrolbev1.repository.InventoryRepository;
import org.luun.kitchencontrolbev1.repository.OrderDetailFillRepository;
import org.luun.kitchencontrolbev1.entity.OrderDetailFill;
import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.service.DeliveryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderDetailFillRepository orderDetailFillRepository;

    @Override
    public List<DeliveryResponse> getDeliveries() {
        List<Delivery> deliveries = deliveryRepository.findAll();
        return deliveries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryResponse> getDeliveriesByShipperId(Integer shipperId) {
        List<Delivery> deliveries = deliveryRepository.getDeliveriesByShipperUserId(shipperId);
        return deliveries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryResponse assignShipperToDelivery(Integer deliveryId, Integer shipperId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));

        User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new RuntimeException("Shipper not found with id: " + shipperId));

        // You might want to check if the user is actually a shipper here
        if (!shipper.getRole().equals("Shipper")) {
            throw new RuntimeException("User is not a shipper");
        }
        delivery.setShipper(shipper);
        Delivery updatedDelivery = deliveryRepository.save(delivery);

        return mapToResponse(updatedDelivery);
    }

    @Override
    @Transactional
    public DeliveryResponse createDeliveryWithOrders(List<Integer> orderIds, Integer shipperId) {
        User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new RuntimeException("Shipper not found with id: " + shipperId));

        if (!shipper.getRole().getRoleName().equals("SHIPPER")) {
            // Assume roleName check, adjust as needed.
        }

        // FLOW 1 - Bước 2: Điều phối & Gom đơn. Tạo chuyến xe (Deliveries) và gán
        // Shipper
        Delivery delivery = new Delivery();
        delivery.setShipper(shipper);
        delivery.setDeliveryDate(LocalDate.now());
        delivery.setCreatedAt(LocalDateTime.now());
        Delivery savedDelivery = deliveryRepository.save(delivery);

        List<Order> ordersToProcess = orderRepository.findAllById(orderIds);
        List<Order> savedOrders = new ArrayList<>();

        for (Order order : ordersToProcess) {
            if (order.getStatus() != OrderStatus.WAITTING) {
                continue; // Chỉ gom những đơn WAITTING
            }
            order.setDelivery(savedDelivery); // Update orders.delivery_id
            order.setStatus(OrderStatus.PROCESSING); // Chuyển status sang PROCESSING

            // FLOW 1 - Bước 3.1: Phân bổ tự động (Logic FEFO)
            // Hệ thống tự động chạy thuật toán tìm hạn sử dụng gần nhất.
            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    Float requiredQuantity = detail.getQuantity();

                    // Quét bảng inventories, tìm các batch_id có hạn sử dụng gần nhất và lớn hơn
                    // ngày hiện tại
                    List<Inventory> inventories = inventoryRepository
                            .findByProductProductIdAndQuantityGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
                                    detail.getProduct().getProductId(), 0f, LocalDate.now());

                    for (Inventory inv : inventories) {
                        if (requiredQuantity <= 0)
                            break; // Hoàn tất số lượng yêu cầu của chi tiết đơn

                        // Check số lượng đã giữ chỗ cho batch này
                        Float filled = orderDetailFillRepository.sumQuantityByBatchIdAndOrderStatuses(
                                inv.getBatch().getBatchId(),
                                Arrays.asList(OrderStatus.WAITTING, OrderStatus.PROCESSING));
                        if (filled == null)
                            filled = 0f;

                        // Tính Available cho Batch này
                        Float available = inv.getQuantity() - filled;
                        if (available > 0) {
                            Float allocateQty = Math.min(requiredQuantity, available);

                            // DB Update: Ghi vào bảng order_detail_fill (mới chỉ giữ chỗ, chưa trừ kho
                            // thật)
                            OrderDetailFill fill = new OrderDetailFill();
                            fill.setOrderDetail(detail);
                            fill.setBatch(inv.getBatch());
                            fill.setQuantity(allocateQty);
                            fill.setCreatedAt(LocalDateTime.now());
                            orderDetailFillRepository.save(fill);

                            requiredQuantity -= allocateQty; // Giảm bớt số lượng cần sau phân bổ
                        }
                    }
                }
            }
            savedOrders.add(orderRepository.save(order));
        }
        savedDelivery.setOrders(savedOrders);

        return mapToResponse(savedDelivery);
    }

    @Override
    @Transactional
    public DeliveryResponse startDelivery(Integer deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));

        // FLOW 1 - Bước 4: Shipper bấm "Bắt đầu đi giao". Tự động đổi orders.status ->
        // DELIVERING
        if (delivery.getOrders() != null) {
            for (Order order : delivery.getOrders()) {
                if (order.getStatus() == OrderStatus.PROCESSING) {
                    order.setStatus(OrderStatus.DELIVERING);
                    orderRepository.save(order);
                }
            }
        }
        return mapToResponse(delivery);
    }

    private DeliveryResponse mapToResponse(Delivery delivery) {
        DeliveryResponse response = new DeliveryResponse();
        response.setDeliveryId(delivery.getDeliveryId());
        response.setDeliveryDate(delivery.getDeliveryDate());
        response.setCreatedAt(delivery.getCreatedAt());

        if (delivery.getShipper() != null) {
            response.setShipperId(delivery.getShipper().getUserId());
            response.setShipperName(delivery.getShipper().getUsername()); // Or fullName if available
        }

        if (delivery.getOrders() != null) {
            List<OrderResponse> orderResponses = delivery.getOrders().stream()
                    .map(this::mapToOrderResponse)
                    .collect(Collectors.toList());
            response.setOrders(orderResponses);
        }

        return response;
    }

    // Helper method to map Order to OrderResponse (duplicated from
    // OrderServiceImpl, consider moving to a Mapper class)
    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());

        if (order.getStore() != null) {
            response.setStoreId(order.getStore().getStoreId());
            response.setStoreName(order.getStore().getStoreName());
        }

        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setImg(order.getImg());
        response.setComment(order.getComment());

        if (order.getOrderDetails() != null) {
            List<OrderDetailResponse> details = order.getOrderDetails().stream()
                    .map(this::mapToDetailResponse)
                    .collect(Collectors.toList());
            response.setOrderDetails(details);
        }

        return response;
    }

    private OrderDetailResponse mapToDetailResponse(OrderDetail detail) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderDetailId(detail.getOrderDetailId());

        if (detail.getProduct() != null) {
            response.setProductId(detail.getProduct().getProductId());
            response.setProductName(detail.getProduct().getProductName());
        }

        response.setQuantity(detail.getQuantity());
        return response;
    }
}
