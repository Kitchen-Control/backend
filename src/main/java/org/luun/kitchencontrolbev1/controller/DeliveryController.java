package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.DeliveryResponse;
import org.luun.kitchencontrolbev1.service.DeliveryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/deliveries")
@Tag(name = "Deliveries API", description = "API for managing deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    @Operation(summary = "Get all deliveries")
    public List<DeliveryResponse> getDeliveries() {
        return deliveryService.getDeliveries();
    }

    @GetMapping("/get-by-shipper/{shipperId}")
    @Operation(summary = "Get deliveries by shipper ID")
    public List<DeliveryResponse> getDeliveriesByShipperId(@PathVariable Integer shipperId) {
        return deliveryService.getDeliveriesByShipperId(shipperId);
    }

    @PatchMapping("/{deliveryId}/assign-shipper/{shipperId}")
    @Operation(summary = "Assign a shipper to a delivery")
    public DeliveryResponse assignShipperToDelivery(
            @PathVariable Integer deliveryId,
            @PathVariable Integer shipperId) {
        return deliveryService.assignShipperToDelivery(deliveryId, shipperId);
    }

    @PostMapping("/create")
    @Operation(summary = "Tạo chuyến xe với danh sách order WAITING và gán Shipper (Chuyển Order -> PROCESSING, và chạy FEFO)")
    public DeliveryResponse createDeliveryWithOrders(
            @RequestParam List<Integer> orderIds,
            @RequestParam Integer shipperId) {
        return deliveryService.createDeliveryWithOrders(orderIds, shipperId);
    }

    @PatchMapping("/{deliveryId}/start")
    @Operation(summary = "Shipper bắt đầu đi giao (Chuyển Order -> DELIVERING)")
    public DeliveryResponse startDelivery(@PathVariable Integer deliveryId) {
        return deliveryService.startDelivery(deliveryId);
    }
}
