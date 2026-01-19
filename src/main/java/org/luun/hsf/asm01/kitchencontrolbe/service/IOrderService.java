package org.luun.hsf.asm01.kitchencontrolbe.service;

import org.luun.hsf.asm01.kitchencontrolbe.dto.request.OrderRequestDTO;
import org.luun.hsf.asm01.kitchencontrolbe.dto.response.OrderResponseDTO;
import java.util.List;

public interface IOrderService {
    OrderResponseDTO createOrder(OrderRequestDTO request);
    OrderResponseDTO getOrderById(int orderId);
    List<OrderResponseDTO> getAllOrders();
    void deleteOrder(int orderId);
}
