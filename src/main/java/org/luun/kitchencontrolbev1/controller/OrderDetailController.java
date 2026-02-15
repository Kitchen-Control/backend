package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.OrderDetailRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.luun.kitchencontrolbev1.service.OrderDetailService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Order Details API")
@RequestMapping("/order-details")
@RequiredArgsConstructor
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    @GetMapping
    @Operation(summary = "Get all order details", description = "Retrieves a list of all order details.")
    public List<OrderDetailResponse> getAllOrderDetails() {
        return orderDetailService.getAllOrderDetails();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order detail by ID", description = "Retrieves a specific order detail by its ID.")
    public OrderDetailResponse getOrderDetailById(@PathVariable Integer id) {
        return orderDetailService.getOrderDetailById(id);
    }

    @PostMapping
    @Operation(summary = "Create a new order detail", description = "Adds a new order detail to the database.")
    public OrderDetailResponse createOrderDetail(@RequestBody OrderDetailRequest request) {
        return orderDetailService.createOrderDetail(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an order detail", description = "Updates the details of an existing order detail identified by its ID.")
    public OrderDetailResponse updateOrderDetail(@PathVariable Integer id, @RequestBody OrderDetail updatedOrderDetail) {
        return orderDetailService.updateOrderDetail(id, updatedOrderDetail);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order detail", description = "Deletes an existing order detail identified by its ID.")
    public void deleteOrderDetail(@PathVariable Integer id) {
        orderDetailService.deleteOrderDetail(id);
    }
}
