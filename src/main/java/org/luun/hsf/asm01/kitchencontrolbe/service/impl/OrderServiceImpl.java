package org.luun.hsf.asm01.kitchencontrolbe.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.hsf.asm01.kitchencontrolbe.dto.request.OrderRequestDTO;
import org.luun.hsf.asm01.kitchencontrolbe.dto.response.OrderResponseDTO;
import org.luun.hsf.asm01.kitchencontrolbe.entity.Order;
import org.luun.hsf.asm01.kitchencontrolbe.entity.OrderDetail;
import org.luun.hsf.asm01.kitchencontrolbe.entity.enums.OrderStatus;
import org.luun.hsf.asm01.kitchencontrolbe.mapper.OrderMapper;
import org.luun.hsf.asm01.kitchencontrolbe.repository.OrderRepository;
import org.luun.hsf.asm01.kitchencontrolbe.service.IOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        Order order = new Order();
        order.setStoreId(request.getStoreId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.WAITTING); //Mặc định ban đầu là waitting

        List<OrderDetail> details = request.getOrderDetails().stream().map(detailDto -> {
            OrderDetail detail = orderMapper.toOrderDetailEntity(detailDto);
            detail.setOrder(order);
            return detail;
        }).collect(Collectors.toList());

        order.setOrderDetails(details);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponseDTO(savedOrder);
    }

    @Override
    public OrderResponseDTO getOrderById(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return orderMapper.toResponseDTO(order);
    }
    
    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(int orderId) {
        orderRepository.deleteById(orderId);
    }
}
