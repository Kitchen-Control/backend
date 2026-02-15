package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.OrderDetailRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.luun.kitchencontrolbev1.repository.OrderDetailRepository;
import org.luun.kitchencontrolbev1.repository.OrderRepository;
import org.luun.kitchencontrolbev1.repository.ProductRepository;
import org.luun.kitchencontrolbev1.service.OrderDetailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public List<OrderDetailResponse> getAllOrderDetails() {
        return orderDetailRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDetailResponse getOrderDetailById(Integer id) {
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found"));
        return mapToResponse(orderDetail);
    }

    @Override
    public OrderDetailResponse createOrderDetail(OrderDetailRequest request) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found")));
        orderDetail.setProduct(productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found")));
        orderDetail.setQuantity(request.getQuantity());
        
        OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);
        return mapToResponse(savedOrderDetail);
    }

    @Override
    public OrderDetailResponse updateOrderDetail(Integer id, OrderDetail updatedOrderDetail) {
        OrderDetail existingOrderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found"));
        
        existingOrderDetail.setQuantity(updatedOrderDetail.getQuantity());
        // Update other fields as necessary

        OrderDetail savedOrderDetail = orderDetailRepository.save(existingOrderDetail);
        return mapToResponse(savedOrderDetail);
    }

    @Override
    public void deleteOrderDetail(Integer id) {
        orderDetailRepository.deleteById(id);
    }

    private OrderDetailResponse mapToResponse(OrderDetail orderDetail) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderDetailId(orderDetail.getOrderDetailId());
        if (orderDetail.getOrder() != null) {
            response.setOrderId(orderDetail.getOrder().getOrderId());
        }
        if (orderDetail.getProduct() != null) {
            response.setProductId(orderDetail.getProduct().getProductId());
        }
        response.setQuantity(orderDetail.getQuantity());
        return response;
    }
}
