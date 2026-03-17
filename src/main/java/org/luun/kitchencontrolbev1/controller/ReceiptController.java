package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.ReceiptStatusUpdateRequest;
import org.luun.kitchencontrolbev1.dto.response.ReceiptResponse;
import org.luun.kitchencontrolbev1.enums.ReceiptStatus;
import org.luun.kitchencontrolbev1.service.ReceiptService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/receipts")
@Tag(name = "Receipts API", description = "API for managing receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get all receipts by Order ID")
    public List<ReceiptResponse> getByOrderId(@PathVariable Integer orderId) {
        return receiptService.getByOrderId(orderId);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get all receipts by Status")
    public List<ReceiptResponse> getByStatus(@PathVariable ReceiptStatus status) {
        return receiptService.getByStatus(status);
    }

    @PostMapping("/order/{orderId}")
    @Operation(summary = "Create a draft receipt for an order")
    public ReceiptResponse createReceipt(
            @PathVariable Integer orderId,
            @RequestParam(required = false) String note) {
        return receiptService.createReceipt(orderId, note);
    }

    @PatchMapping("/status")
    @Operation(summary = "Update receipts status (Use confirmReceipt for COMPLETED)")
    public void updateReceiptStatus(@RequestBody ReceiptStatusUpdateRequest request) {
        receiptService.updateReceiptStatus(request.getReceiptIds(), request.getStatus());
    }

    @DeleteMapping("/{receiptId}")
    @Operation(summary = "Delete a DRAFT receipt")
    public void deleteReceipt(@PathVariable Integer receiptId) {
        receiptService.deleteReceipt(receiptId);
    }
}
