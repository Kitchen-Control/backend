package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.InventoryTransactionRequest;
import org.luun.kitchencontrolbev1.dto.response.InventoryTransactionResponse;

import java.util.List;

public interface InventoryTransactionService {
    List<InventoryTransactionResponse> getAllTransactions();
    List<InventoryTransactionResponse> getTransactionsByProductId(Integer productId);
    List<InventoryTransactionResponse> getTransactionsByBatchId(Integer batchId);
    InventoryTransactionResponse createTransaction(InventoryTransactionRequest request);
}
