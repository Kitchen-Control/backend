package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.OrderDetailRequest;
import org.luun.kitchencontrolbev1.dto.request.OrderRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailFillResponse;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import org.luun.kitchencontrolbev1.entity.*;
import org.luun.kitchencontrolbev1.enums.DeliveryStatus;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.repository.*;
import org.luun.kitchencontrolbev1.service.*;
import org.luun.kitchencontrolbev1.service.statustransitionhandler.OrderStatusTransitionHandler;
import org.luun.kitchencontrolbev1.service.statusvalidator.OrderStatusValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserService userService;
    private final ProductService productService;
    private final StoreService storeService;

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
                .allMatch(o ->
                        o.getStatus() == OrderStatus.DONE ||
                                o.getStatus() == OrderStatus.DAMAGED ||
                                o.getStatus() == OrderStatus.PARTIAL_DELIVERED
                );

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
