package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    //Get full orders
    @Operation(summary = "Get all orders")
    @GetMapping
    public List<OrderResponse> getOrders() {
        return orderService.getOrders();
    }

    //Get orders by store_id
    @Operation(summary = "Get orders by store_id")
    @GetMapping("/get-by-store/{storeId}")
    public List<OrderResponse> getOrdersByStoreId(@PathVariable Integer storeId) {
        return orderService.getOrdersByStoreId(storeId);
    }

    //Create a new order
    @PostMapping
    @Operation(summary = "Create order")
    public OrderResponse createOrder(
            @Parameter(description = "Order details", required = true) @RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    //Updating status of an order
    @PatchMapping("/update-status")
    @Operation(summary = "Update order status", description = "Update the status of an order")
    public OrderResponse updateOrderStatus(@RequestParam Integer orderId, @RequestParam OrderStatus status) {
        return orderService.updateOrderStatus(orderId, status);
    }
}
