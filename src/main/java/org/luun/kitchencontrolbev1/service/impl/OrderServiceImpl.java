package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.luun.kitchencontrolbev1.repository.OrderRepository;
import org.luun.kitchencontrolbev1.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public List<OrderResponse> getOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByStoreId(Integer storeId) {
        List<Order> orders = orderRepository.findByStoreStoreId(storeId);
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
        
        if (detail.getProduct() != null) {
            response.setProductId(detail.getProduct().getProductId());
            response.setProductName(detail.getProduct().getProductName());
        }
        
        response.setQuantity(detail.getQuantity());
        return response;
    }
}
