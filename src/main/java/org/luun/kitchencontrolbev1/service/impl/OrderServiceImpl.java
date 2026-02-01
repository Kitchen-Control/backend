package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.OrderDetailRequest;
import org.luun.kitchencontrolbev1.dto.request.OrderRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.entity.Store;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.repository.OrderDetailRepository;
import org.luun.kitchencontrolbev1.repository.OrderRepository;
import org.luun.kitchencontrolbev1.repository.ProductRepository;
import org.luun.kitchencontrolbev1.repository.StoreRepository;
import org.luun.kitchencontrolbev1.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;

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

    //Creating orders method
    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        // 1. Create and save Order
        Order order = new Order();
        
        // Set Store
        Store store = storeRepository.findById(orderRequest.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + orderRequest.getStoreId()));
        order.setStore(store);
        
        // Set basic info
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.WAITTING); // Default status
        order.setComment(orderRequest.getComment());
        
        Order savedOrder = orderRepository.save(order);
        
        // 2. Create and save OrderDetails
        List<OrderDetail> orderDetails = new ArrayList<>();
        if (orderRequest.getOrderDetails() != null) {
            for (OrderDetailRequest detailRequest : orderRequest.getOrderDetails()) {
                OrderDetail detail = new OrderDetail();
                detail.setOrder(savedOrder);
                
                Product product = productRepository.findById(detailRequest.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + detailRequest.getProductId()));
                detail.setProduct(product);
                
                detail.setQuantity(detailRequest.getQuantity());
                
                orderDetails.add(orderDetailRepository.save(detail));
            }
        }
        
        savedOrder.setOrderDetails(orderDetails);

        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
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
