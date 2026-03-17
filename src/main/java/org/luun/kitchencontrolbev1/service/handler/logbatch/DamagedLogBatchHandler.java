package org.luun.kitchencontrolbev1.service.handler.logbatch;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.entity.InventoryTransaction;
import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.entity.WasteLog;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.repository.WasteLogRepository;
import org.luun.kitchencontrolbev1.service.InventoryService;
import org.luun.kitchencontrolbev1.service.InventoryTransactionService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DamagedLogBatchHandler implements LogBatchStatusHandler{

    private final InventoryService inventoryService;
    private final InventoryTransactionService inventoryTransactionService;
    private final WasteLogRepository wasteLogRepository;

    @Override
    public LogBatchStatus supportedStatus() {
        return LogBatchStatus.DAMAGED;
    }

    @Override
    public void handle(LogBatch batch) {
        // 1. Kiểm tra xem đã có Inventory cho lô này chưa
        Inventory inventory = inventoryService.getInventoryByBatchId(batch.getBatchId());

        // 2. kiểm tra Inventory còn hàng k
        if (!(inventory.getQuantity() > 0)) {
            throw new RuntimeException("Inventory is empty");
        }

        // 3. Tạo Transaction log (Xuất kho do hàng hết hạn hoặc hỏng)
        InventoryTransaction trans = inventoryTransactionService.createExportTransaction(batch,
                inventory.getQuantity(), "Thủ kho xác nhận xuất hủy hàng hết hạn hoặc bị hỏng");

        inventory.setQuantity((float) 0);

        // Creating report
        WasteLog wasteLog = new WasteLog();
        wasteLog.setBatch(batch);
        wasteLog.setProduct(batch.getProduct());
        wasteLog.setQuantity(batch.getQuantity());
        wasteLog.setWasteType(supportedStatus().name());
        wasteLog.setCreatedAt(LocalDateTime.now());
        wasteLog.setNote("This waste log is automatically generated when log batch is damaged!!");
        wasteLogRepository.save(wasteLog);
    }
}
