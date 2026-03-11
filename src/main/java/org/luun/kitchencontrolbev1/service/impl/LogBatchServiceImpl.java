package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.luun.kitchencontrolbev1.dto.request.LogBatchRequest;
import org.luun.kitchencontrolbev1.dto.response.LogBatchResponse;
import org.luun.kitchencontrolbev1.entity.*;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.enums.LogBatchType;
import org.luun.kitchencontrolbev1.repository.*;
import org.luun.kitchencontrolbev1.service.InventoryService;
import org.luun.kitchencontrolbev1.service.LogBatchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogBatchServiceImpl implements LogBatchService {

    private final LogBatchRepository logBatchRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ReportRepository reportRepository;

    @Override
    public List<LogBatchResponse> getAllLogBatches() {
        return logBatchRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LogBatchResponse getLogBatchById(Integer batchId) {
        LogBatch logBatch = logBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("LogBatch not found with id: " + batchId));
        return mapToResponse(logBatch);
    }

    @Override
    public List<LogBatchResponse> getLogBatchesByPlanId(Integer planId) {
        return logBatchRepository.findByPlan_PlanId(planId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LogBatchResponse> getLogBatchesByProductId(Integer productId) {
        return logBatchRepository.findByProduct_ProductId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LogBatchResponse> getLogBatchesByStatus(LogBatchStatus status) {
        return logBatchRepository.findByStatus(status.name()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LogBatchResponse createProductionLogBatch(LogBatchRequest request) {
        LogBatch logBatch = new LogBatch();

        // 1. Gắn Production Plan
        if (request.getPlanId() != null) {
            ProductionPlan plan = productionPlanRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new RuntimeException("Production Plan not found with id: " + request.getPlanId()));
            logBatch.setPlan(plan);
        } else {
            throw new RuntimeException("Plan ID is required for PRODUCTION type");
        }

        // 2. Gắn Product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));
        logBatch.setProduct(product);

        // 3. Set các thuộc tính khác
        logBatch.setQuantity(request.getQuantity());
        logBatch.setProductionDate(request.getProductionDate());
        logBatch.setExpiryDate(LocalDate.now().plusDays(product.getShelfLifeDays()));
        logBatch.setCreatedAt(LocalDateTime.now());
        logBatch.setStatus(LogBatchStatus.PROCESSING);

        if (request.getType() == null || request.getType() != LogBatchType.PRODUCTION) {
            throw new RuntimeException("Type is required or must be PRODUCTION");
        }

        logBatch.setType(request.getType());
        LogBatch savedBatch = logBatchRepository.save(logBatch);

        return mapToResponse(savedBatch);
    }

    @Override
    @Transactional
    public LogBatchResponse createPurchaseLogBatch(LogBatchRequest request) {
        LogBatch logBatch = new LogBatch();

        // Gắn Product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));
        logBatch.setProduct(product);

        // Set các thuộc tính khác
        logBatch.setQuantity(request.getQuantity());
        logBatch.setProductionDate(request.getProductionDate());
        logBatch.setExpiryDate(request.getExpiryDate());
        logBatch.setCreatedAt(LocalDateTime.now());
        logBatch.setStatus(LogBatchStatus.DONE);

        if (request.getType() == null || request.getType() != LogBatchType.PURCHASE) {
            throw new RuntimeException("Type is required or must be PURCHASE");
        }

        logBatch.setType(request.getType());
        LogBatch savedBatch = logBatchRepository.save(logBatch);

        handleBatchDone(savedBatch);

        return mapToResponse(savedBatch);
    }

    @Override
    @Transactional
    public LogBatchResponse updateLogBatchStatus(Integer batchId, LogBatchStatus newStatus) {
        // 1. Lấy LogBatch từ DB
        LogBatch logBatch = logBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("LogBatch not found"));

        // 2. Kiểm tra logic chuyển đổi trạng thái
        if(!validateStatusTransition(logBatch.getStatus(), newStatus)) {
            throw new RuntimeException("Invalid status transition");
        }

        // 3. Xử lý logic riêng cho từng trạng thái
        switch (newStatus) {
            case DONE:
                handleBatchDone(logBatch);
                break;
            case DAMAGED:
                handleBatchDamaged(logBatch);
                break;
            default:
                // Các trạng thái khác chỉ cần update status bình thường
                break;
        }

        // 4. Cập nhật status và lưu
        logBatch.setStatus(newStatus);
        return mapToResponse(logBatchRepository.save(logBatch));
    }

    private Boolean validateStatusTransition(LogBatchStatus currentStatus, LogBatchStatus newStatus) {

        // handle DAMAGED status
        if (currentStatus == LogBatchStatus.DAMAGED) {
            throw new IllegalStateException("Cannot change status from " + currentStatus);
        }

        // handle PROCESSING status
        if (currentStatus == LogBatchStatus.PROCESSING
                && (newStatus == LogBatchStatus.DONE || newStatus == LogBatchStatus.DAMAGED)) {
            throw new IllegalStateException("Cannot change status from PROCESSING to DONE or DAMAGED");
        }

        // handle DONE status
        if (currentStatus == LogBatchStatus.DONE && newStatus != LogBatchStatus.WAITING_TO_CANCEL) {
            throw new IllegalStateException("Cannot change status from DONE to " + newStatus);
        }

        // handle WAITING_TO_CONFIRM status
        if (currentStatus == LogBatchStatus.WAITING_TO_CONFIRM && newStatus != LogBatchStatus.DONE) {
            throw new IllegalStateException("Cannot change status from WAITING_TO_CONFIRM to " + newStatus);
        }

        // handle WAITING_TO_CANCLE status
        if (currentStatus == LogBatchStatus.DONE && newStatus != LogBatchStatus.WAITING_TO_CANCEL) {
            throw new IllegalStateException("Cannot change status from DONE to " + newStatus);
        }

        return true;
    }

    @Override
    @Transactional
    public void expireLogBatch(Integer batchId) {
        // 1. Finding LogBatch
        LogBatch logBatch = logBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("LogBatch not found with id: " + batchId));

        // 2. Just allow log batch status is WAITING_TO_CANCLE
        if (logBatch.getStatus() != LogBatchStatus.WAITING_TO_CANCEL) throw new RuntimeException("LogBatch status must be WAITING_TO_CANCLE");

        // 3. Set status = DAMAGED
        logBatch.setStatus(LogBatchStatus.DAMAGED);
        logBatchRepository.save(logBatch);

        // 4. Finding inventory CỦA CHÍNH LÔ NÀY → set quantity = 0
        Inventory inventory = inventoryRepository.findByBatchBatchId(batchId)
                .orElseThrow(() -> new RuntimeException("Inventory not found with batchId: " + batchId));
        float disposedQuantity = inventory.getQuantity();
        inventory.setQuantity(0f);
        inventoryRepository.save(inventory);

        // 5. Creating inventory_transaction with note
        InventoryTransaction tx = new InventoryTransaction();
        tx.setType(InventoryTransactionType.EXPORT);
        tx.setQuantity(disposedQuantity);
        tx.setNote("Thủ kho xác nhận xuất hủy hàng hết hạn");
        tx.setBatch(logBatch);
        tx.setProduct(logBatch.getProduct());
        inventoryTransactionRepository.save(tx);

        Report report = new Report();
        report.setReportType("WASTE");
        report.setCreatedDate(LocalDateTime.now());
        report.setUser(null);
        reportRepository.save(report);
    }

    /**
     * This method is scheduled to run automatically to check for expired batches.
     * It finds batches that are expired and not yet marked as WAITING_TO_CANCLE or EXPIRED,
     * and updates their status to WAITING_TO_CANCLE.
     */
    @Scheduled(cron = "0 1 0  * * ?", zone = "Asia/Ho_Chi_Minh") // Runs every day at 1:00 AM and in Vietnam time
    @Transactional
    public void updateExpiredBatches() {
        log.warn("Checking for expired batches...");
        LocalDate today = LocalDate.now();

        // Find batches that are expired (expiry_date < today) and have a status that can be changed
        // (e.g., DONE, PROCESSING). We don't want to change batches that are already handled.
        List<LogBatch> expiredBatches = logBatchRepository.findByExpiryDateBeforeAndStatusIn(
                today,
                List.of(LogBatchStatus.DONE.name())
        );
        if(expiredBatches.isEmpty()) {
            log.warn("No expired batches found.");
            return;
        }
        for (LogBatch batch : expiredBatches) {
            batch.setStatus(LogBatchStatus.WAITING_TO_CANCEL);
            logBatchRepository.save(batch);
            // You might want to add logging here to record which batches were updated
            log.warn("Batch ID " + batch.getBatchId() + " has expired and status updated to WAITING_TO_CANCLE.");
        }
    }
    @Transactional
    protected void handleBatchDone(LogBatch logBatch) {

        // 1. Kiểm tra xem đã có Inventory cho lô này chưa (tránh cộng dồn 2 lần nếu lỡ bấm update 2 lần)
        if (inventoryRepository.findByBatchBatchId(logBatch.getBatchId()).isPresent()) {
            throw new RuntimeException("Inventory already exists for this batch"); // Hoặc throw exception tùy nghiệp vụ
        }

        // 2. Tạo Inventory mới
        Inventory inventory = new Inventory();
        inventory.setProduct(logBatch.getProduct());
        inventory.setBatch(logBatch);
        inventory.setQuantity(logBatch.getQuantity()); // Số lượng thực tế sản xuất
        inventory.setExpiryDate(logBatch.getExpiryDate());
        inventoryRepository.save(inventory);

        // 3. Tạo Transaction log (Nhập kho từ sản xuất/hàng mua)
        InventoryTransaction trans = new InventoryTransaction();
        trans.setProduct(logBatch.getProduct());
        trans.setBatch(logBatch);
        trans.setType(InventoryTransactionType.IMPORT);
        trans.setQuantity(logBatch.getQuantity());
        trans.setCreatedAt(LocalDateTime.now());

        inventoryTransactionRepository.save(trans);
    }

    @Transactional
    protected void handleBatchDamaged(LogBatch logBatch) {

        // 1. Kiểm tra xem đã có Inventory cho lô này chưa
        Inventory inventory = inventoryRepository.findByBatchBatchId(logBatch.getBatchId())
                .orElseThrow(() -> new RuntimeException("Inventory not found for batch: " + logBatch.getBatchId()));

        // 2. kiểm tra Inventory còn hàng k
        if(!(inventory.getQuantity() > 0)) {
            throw new RuntimeException("Inventory is empty");
        }

        // 3. Tạo Transaction log (Xuất kho do hàng hết hạn hoặc hỏng)
        InventoryTransaction trans = new InventoryTransaction();
        trans.setProduct(logBatch.getProduct());
        trans.setBatch(logBatch);
        trans.setType(InventoryTransactionType.EXPORT);
        trans.setQuantity(inventory.getQuantity());
        trans.setCreatedAt(LocalDateTime.now());
        trans.setNote("Hủy hàng");
        inventoryTransactionRepository.save(trans);

        inventory.setQuantity((float) 0);
        inventoryRepository.save(inventory);
    }

    private LogBatchResponse mapToResponse(LogBatch batch) {
        LogBatchResponse response = new LogBatchResponse();
        response.setBatchId(batch.getBatchId());

        if (batch.getPlan() != null) {
            response.setPlanId(batch.getPlan().getPlanId());
        }

        if (batch.getProduct() != null) {
            response.setProductId(batch.getProduct().getProductId());
            response.setProductName(batch.getProduct().getProductName());
        }

        response.setQuantity(batch.getQuantity());
        response.setProductionDate(batch.getProductionDate());
        response.setExpiryDate(batch.getExpiryDate());
        response.setStatus(batch.getStatus());
        response.setType(batch.getType());
        response.setCreatedAt(batch.getCreatedAt());

        return response;
    }
}
