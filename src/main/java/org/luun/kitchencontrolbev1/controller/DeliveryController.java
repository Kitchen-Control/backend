package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.AssignShipperRequest;
import org.luun.kitchencontrolbev1.dto.response.DeliveryResponse;
import org.luun.kitchencontrolbev1.enums.DeliveryStatus;
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

    @PostMapping("")
    @Operation(summary = "Create delivery and assign orders, shipper to it")
    public void createDelivery(@RequestBody AssignShipperRequest assignShipperRequest) {
        deliveryService.createDelivery(assignShipperRequest);
    }

    @PatchMapping("/{deliveryId}/status")
    @Operation(summary = "Update delivery status (WAITING -> DELIVERING)")
    public void updateDeliveryStatus(
            @PathVariable Integer deliveryId,
            @RequestParam DeliveryStatus status) {
        deliveryService.updateDeliveryStatus(deliveryId, status);
    }
}
