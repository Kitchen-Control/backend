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
    public List<DeliveryResponse> getDeliveriesByShipperId(@PathVariable("shipperId") Integer shipperId) {
        return deliveryService.getDeliveriesByShipperId(shipperId);
    }

    @GetMapping("/getting-by-status/{status}")
    @Operation(summary = "Get deliveries by status")
    public List<DeliveryResponse> getDeliveriesByStatus(@PathVariable("status") DeliveryStatus status) {
        return deliveryService.getDeliveriesByStatus(status);
    }

    @PostMapping("/create")
    @Operation(summary = "Create delivery and assign orders, shipper to it")
    public DeliveryResponse createDelivery(@RequestBody AssignShipperRequest assignShipperRequest) {
        return deliveryService.assignShipperToDelivery(assignShipperRequest);
    }

    @PatchMapping("/{deliveryId}/start")
    @Operation(summary = "Shipper starts the delivery trip -> Orders status becomes DELIVERING")
    public DeliveryResponse startDelivery(@PathVariable Integer deliveryId) {
        return deliveryService.startDelivery(deliveryId);
    }

    @PatchMapping("/{deliveryId}/status")
    @Operation(summary = "Update delivery status (WAITING -> DELIVERING -> DONE)")
    public DeliveryResponse updateDeliveryStatus(
            @PathVariable Integer deliveryId,
            @RequestParam DeliveryStatus status) {
        return deliveryService.updateDeliveryStatus(deliveryId, status);
    }
}
