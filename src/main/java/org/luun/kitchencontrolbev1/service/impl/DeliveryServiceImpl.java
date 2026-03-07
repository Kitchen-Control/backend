package org.luun.kitchencontrolbev1.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.AssignShipperRequest;
import org.luun.kitchencontrolbev1.dto.response.DeliveryResponse;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import org.luun.kitchencontrolbev1.entity.Delivery;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.luun.kitchencontrolbev1.entity.User;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.repository.DeliveryRepository;
import org.luun.kitchencontrolbev1.repository.OrderRepository;
import org.luun.kitchencontrolbev1.repository.UserRepository;
import org.luun.kitchencontrolbev1.service.DeliveryService;
import org.luun.kitchencontrolbev1.service.OrderDetailFillService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailFillService orderDetailFillService;

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
    @Transactional
    public DeliveryResponse assignShipperToDelivery(AssignShipperRequest request) {
        if (request.getOrderIds() == null || request.getOrderIds().isEmpty()) {
            throw new RuntimeException("There are no orders to assign");
        }

        User shipper = userRepository.findById(request.getShipperId())
                .orElseThrow(() -> new RuntimeException("Shipper not found with id: " + request.getShipperId()));
        if (!shipper.getRole().getRoleName().equals("SHIPPER")) {
            throw new RuntimeException("User is not a shipper");
        }

        Delivery delivery = new Delivery();
        delivery.setShipper(shipper);
        delivery.setDeliveryDate(request.getDeliveryDate());
        delivery.setCreatedAt(LocalDateTime.now());

        List<Order> orders = orderRepository.findAllById(request.getOrderIds());
        if (orders.size() != request.getOrderIds().size()) {
            throw new RuntimeException("Some order IDs are invalid or not found");
        }

        deliveryRepository.save(delivery);

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.WAITTING) {
                order.setDelivery(delivery);
                order.setStatus(OrderStatus.PROCESSING); // Chuyển sang PROCESSING

                // Giai đoạn 3.1: Khi đơn hàng chuyển sang PROCESSING, kích hoạt tự động chạy
                // thuật toán FEFO
                orderDetailFillService.autoAllocateFEFO(order.getOrderId());
            } else {
                throw new RuntimeException("There is a order that is not waiting");
            }
        }

        delivery.setOrders(orders);
        return mapToResponse(delivery);
    }

    @Override
    @Transactional
    // Bước 4: Giao hàng (Shipping) - App shipper bấm "Bắt đầu đi giao"
    public DeliveryResponse startDelivery(Integer deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));

        List<Order> orders = delivery.getOrders();
        // Kiểm tra xem có order nào để đi giao không
        if (orders == null || orders.isEmpty()) {
            throw new RuntimeException("Delivery does not contain any orders");
        }

        // Tự động chuyển các đơn thuộc chuyến đó orders.status -> DELIVERING
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PROCESSING) {
                order.setStatus(OrderStatus.DELIVERING);
            } else {
                // Có thể throw hoặc bỏ qua tuỳ logic, nhưng yêu cầu đề bài chuyển các đơn
                throw new RuntimeException(
                        "Order " + order.getOrderId() + " is not ready for delivery (Not PROCESSING).");
            }
        }

        return mapToResponse(deliveryRepository.save(delivery));
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
