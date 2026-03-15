package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.OrderRequest;
import org.luun.kitchencontrolbev1.dto.request.OrderStatusUpdateRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.service.OrderDetailFillService;
import org.luun.kitchencontrolbev1.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Orders API", description = "API for managing orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderDetailFillService orderDetailFillService;

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

    @GetMapping("/filter-by-status")
    @Operation(summary = "Get orders by status", description = "Get orders by status (WAITTING, PROCESSING, DELIVERING, DISPATCHED, DONE, DAMAGED, CANCLED)")
    public List<OrderResponse> getOrdersByStatus(@RequestParam OrderStatus status) {
        return orderService.getOrdersByStatus(status);
    }

    @GetMapping("/get-by-shipper/{shipperId}")
    @Operation(summary = "Get orders by shipper ID")
    public List<OrderResponse> getOrdersByShipperId(@PathVariable Integer shipperId) {
        return orderService.getOrdersByShipperId(shipperId);
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @PatchMapping("/update-status")
    @Operation(summary = "Update orders status")
    public void updateOrderStatus(@RequestBody OrderStatusUpdateRequest request) {
        orderService.updateOrderStatus(request.getOrderIds(), request.getStatus());
    }
}
