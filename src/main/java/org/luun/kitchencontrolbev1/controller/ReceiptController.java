package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.ReceiptResponse;
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
}
