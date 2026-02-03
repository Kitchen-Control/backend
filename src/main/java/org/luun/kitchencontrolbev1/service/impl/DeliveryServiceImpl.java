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
import org.luun.kitchencontrolbev1.service.DeliveryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

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

    // Helper method to map Order to OrderResponse (duplicated from OrderServiceImpl, consider moving to a Mapper class)
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
