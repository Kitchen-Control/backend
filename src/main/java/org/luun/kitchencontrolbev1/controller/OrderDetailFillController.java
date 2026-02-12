package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.OrderDetailFillRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailFillResponse;
import org.luun.kitchencontrolbev1.entity.OrderDetailFill;
import org.luun.kitchencontrolbev1.service.OrderDetailFillService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Order Detail Fills API")
@RequestMapping("/order-detail-fills")
@RequiredArgsConstructor
public class OrderDetailFillController {

    private final OrderDetailFillService orderDetailFillService;

    @GetMapping
    @Operation(summary = "Get all order detail fills", description = "Retrieves a list of all order detail fills.")
    public List<OrderDetailFillResponse> getAllOrderDetailFills() {
        return orderDetailFillService.getAllOrderDetailFills();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order detail fill by ID", description = "Retrieves a specific order detail fill by its ID.")
    public OrderDetailFillResponse getOrderDetailFillById(@PathVariable Integer id) {
        return orderDetailFillService.getOrderDetailFillById(id);
    }

    @PostMapping
    @Operation(summary = "Create a new order detail fill", description = "Adds a new order detail fill to the database.")
    public OrderDetailFillResponse createOrderDetailFill(@RequestBody OrderDetailFillRequest request) {
        return orderDetailFillService.createOrderDetailFill(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an order detail fill", description = "Updates the details of an existing order detail fill identified by its ID.")
    public OrderDetailFillResponse updateOrderDetailFill(@PathVariable Integer id, @RequestBody OrderDetailFill updatedOrderDetailFill) {
        return orderDetailFillService.updateOrderDetailFill(id, updatedOrderDetailFill);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order detail fill", description = "Deletes an existing order detail fill identified by its ID.")
    public void deleteOrderDetailFill(@PathVariable Integer id) {
        orderDetailFillService.deleteOrderDetailFill(id);
    }
}
