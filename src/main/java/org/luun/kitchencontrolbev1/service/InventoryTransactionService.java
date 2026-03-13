package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.InventoryTransactionRequest;
import org.luun.kitchencontrolbev1.dto.response.InventoryTransactionResponse;
import org.luun.kitchencontrolbev1.entity.InventoryTransaction;
import org.luun.kitchencontrolbev1.entity.LogBatch;

import java.util.List;

public interface InventoryTransactionService {
    List<InventoryTransactionResponse> getAllTransactions();
    List<InventoryTransactionResponse> getTransactionsByProductId(Integer productId);
    List<InventoryTransactionResponse> getTransactionsByBatchId(Integer batchId);
    InventoryTransactionResponse createTransaction(InventoryTransactionRequest request);
    InventoryTransaction createImportTransaction(LogBatch batch, String note);
    InventoryTransaction createExportTransaction(LogBatch batch, Float quantity, String note);
}
