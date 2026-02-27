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
@Tag(name = "Receipts API", description = "API for managing warehouse receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping("/create")
    @Operation(summary = "Tạo phiếu xuất kho (DRAFT) cho đơn hàng")
    public ReceiptResponse createReceiptForOrder(@RequestParam Integer orderId,
            @RequestParam(required = false) String note) {
        return receiptService.createReceiptForOrder(orderId, note);
    }

    @PatchMapping("/complete/{receiptId}")
    @Operation(summary = "Hoàn tất phiếu xuất kho (COMPLETED), trừ kho và tạo transaction")
    public ReceiptResponse completeReceipt(@PathVariable Integer receiptId) {
        return receiptService.completeReceipt(receiptId);
    }

    @GetMapping("/by-order/{orderId}")
    @Operation(summary = "Lấy danh sách phiếu xuất theo Order ID")
    public List<ReceiptResponse> getReceiptsByOrderId(@PathVariable Integer orderId) {
        return receiptService.getReceiptsByOrderId(orderId);
    }
}
