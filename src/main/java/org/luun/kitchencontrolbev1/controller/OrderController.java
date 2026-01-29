package org.luun.kitchencontrolbev1.controller;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    //Get full orders
    @GetMapping
    public List<OrderResponse> getOrders() {
        return orderService.getOrders();
    }

    //Get orders by store_id
    @GetMapping("/get-by-store/{storeId}")
    public List<OrderResponse> getOrdersByStoreId(@PathVariable Integer storeId) {
        return orderService.getOrdersByStoreId(storeId);
    }

    //Create a new order
    @PostMapping
    public OrderResponse createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    //Updating status of an order
    @PatchMapping("/update-status")
    public OrderResponse updateOrderStatus(@RequestParam Integer orderId, @RequestParam OrderStatus status) {
        return orderService.updateOrderStatus(orderId, status);
    }
}
