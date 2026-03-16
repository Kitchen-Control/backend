package org.luun.kitchencontrolbev1.service.handler.logbatch;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.entity.InventoryTransaction;
import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.entity.Report;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.repository.ReportRepository;
import org.luun.kitchencontrolbev1.service.InventoryService;
import org.luun.kitchencontrolbev1.service.InventoryTransactionService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DamagedLogBatchHandler implements LogBatchStatusHandler{

    private final InventoryService inventoryService;
    private final InventoryTransactionService inventoryTransactionService;
    private final ReportRepository reportRepository;

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
        Report report = new Report();
        report.setReportType("WASTE");
        report.setCreatedDate(LocalDateTime.now());
        report.setUser(null);
        reportRepository.save(report);
    }
}
