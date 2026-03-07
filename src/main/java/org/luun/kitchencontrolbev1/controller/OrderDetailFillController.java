package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailFillResponse;
import org.luun.kitchencontrolbev1.service.OrderDetailFillService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order-detail-fills")
@Tag(name = "Order Detail Fills API", description = "API for managing order detail fills")
public class OrderDetailFillController {

    private final OrderDetailFillService orderDetailFillService;

    @GetMapping
    @Operation(summary = "Get all order detail fills")
    public List<OrderDetailFillResponse> getAllOrderDetailFills() {
        return orderDetailFillService.getAllOrderDetailFills();
    }

    @GetMapping("/{fillId}")
    @Operation(summary = "Get order detail fill by ID")
    public OrderDetailFillResponse getOrderDetailFillById(@PathVariable Integer fillId) {
        return orderDetailFillService.getOrderDetailFillById(fillId);
    }

    @GetMapping("/order-detail/{orderDetailId}")
    @Operation(summary = "Get order detail fills by Order Detail ID")
    public List<OrderDetailFillResponse> getOrderDetailFillsByOrderDetailId(@PathVariable Integer orderDetailId) {
        return orderDetailFillService.getOrderDetailFillsByOrderDetailId(orderDetailId);
    }

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "Get order detail fills by Batch ID")
    public List<OrderDetailFillResponse> getOrderDetailFillsByBatchId(@PathVariable Integer batchId) {
        return orderDetailFillService.getOrderDetailFillsByBatchId(batchId);
    }
}
