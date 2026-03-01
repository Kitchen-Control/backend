package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.OrderRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Orders API", description = "API for managing orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get all orders")
    public List<OrderResponse> getOrders() {
        return orderService.getOrders();
    }

    @GetMapping("/get-by-store/{storeId}")
    @Operation(summary = "Get orders by store ID")
    public List<OrderResponse> getOrdersByStoreId(@PathVariable Integer storeId) {
        return orderService.getOrdersByStoreId(storeId);
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @PatchMapping("/update-status/{storeId}")
    @Operation(summary = "Update order status")
    public OrderResponse updateOrderStatus(@RequestParam Integer orderId, @RequestParam("status") OrderStatus status) {
        return orderService.updateOrderStatus(orderId, status);
    }
}
