package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import org.luun.kitchencontrolbev1.service.OrderDetailService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order-details")
@Tag(name = "Order Details API", description = "API for managing order details")
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get all order details by Order ID")
    public List<OrderDetailResponse> getByOrderId(@PathVariable Integer orderId) {
        return orderDetailService.getByOrderId(orderId);
    }
}
