package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.InventoryTransactionRequest;
import org.luun.kitchencontrolbev1.dto.response.InventoryTransactionResponse;
import org.luun.kitchencontrolbev1.service.InventoryTransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory-transactions")
@Tag(name = "Inventory Transactions API", description = "API for managing inventory transactions (Import/Export)")
public class InventoryTransactionController {

    private final InventoryTransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get all transactions")
    public List<InventoryTransactionResponse> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get transactions by product ID")
    public List<InventoryTransactionResponse> getTransactionsByProductId(@PathVariable Integer productId) {
        return transactionService.getTransactionsByProductId(productId);
    }

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "Get transactions by batch ID")
    public List<InventoryTransactionResponse> getTransactionsByBatchId(@PathVariable Integer batchId) {
        return transactionService.getTransactionsByBatchId(batchId);
    }

    @PostMapping
    @Operation(summary = "Create a new transaction (Import/Export)")
    public InventoryTransactionResponse createTransaction(@RequestBody InventoryTransactionRequest request) {
        return transactionService.createTransaction(request);
    }
}
