package org.luun.kitchencontrolbev1.service.handler.logbatch;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.entity.InventoryTransaction;
import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.service.InventoryService;
import org.luun.kitchencontrolbev1.service.InventoryTransactionService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DoneLogBatchHandler implements LogBatchStatusHandler{

    private final InventoryService inventoryService;
    private final InventoryTransactionService inventoryTransactionService;

    @Override
    public LogBatchStatus supportedStatus() {
        return LogBatchStatus.DONE;
    }

    @Override
    public void handle(LogBatch batch) {

        // Tạo Inventory mới
        Inventory inventory = inventoryService.createInventoryFromBatch(batch);

        // Tạo Transaction log (Nhập kho từ sản xuất/hàng mua)
        InventoryTransaction inv = inventoryTransactionService.createImportTransaction(batch, "Lô Nhập");

        batch.setInventory(inventory);
        batch.addInventoryTransaction(inv);
    }
}
