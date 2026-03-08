package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.LogBatchRequest;
import org.luun.kitchencontrolbev1.dto.response.LogBatchResponse;
import org.luun.kitchencontrolbev1.entity.*;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.enums.LogBatchType;
import org.luun.kitchencontrolbev1.repository.*;
import org.luun.kitchencontrolbev1.service.LogBatchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogBatchServiceImpl implements LogBatchService {

    private final LogBatchRepository logBatchRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

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
        return logBatchRepository.findByStatus(status).stream()
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
    public LogBatchResponse updateLogBatchStatus(Integer batchId, LogBatchStatus status) {
        // 1. Lấy LogBatch từ DB
        LogBatch logBatch = logBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("LogBatch not found"));

        // 2. Kiểm tra logic chuyển đổi trạng thái (Optional)
        // Ví dụ: Không thể chuyển từ CANCELLED về PROCESSING
//        validateStatusTransition(logBatch.getStatus(), newStatus);

        // 3. Xử lý logic riêng cho từng trạng thái
        switch (status) {
            case DONE:
                handleBatchDone(logBatch);
                break;
            default:
                // Các trạng thái khác chỉ cần update status bình thường
                break;
        }

        // 4. Cập nhật status và lưu
        logBatch.setStatus(status);
        return mapToResponse(logBatchRepository.save(logBatch));
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

//    @Transactional
//    protected void handleBatchExpired(LogBatch logBatch) {
//
//        // 1. Kiểm tra xem đã có Inventory cho lô này chưa
//        Inventory inventory = inventoryRepository.findByBatchBatchId(logBatch.getBatchId())
//                .orElseThrow(() -> new RuntimeException("Inventory not found for batch: " + logBatch.getBatchId()));
//
//        // 2. kiểm tra Inventory còn hàng k
//        if(!(inventory.getQuantity() > 0)) {
//            throw new RuntimeException("Inventory is empty");
//        }
//
//        // 3. Tạo Transaction log (Xuất kho do hàng hết hạn)
//        InventoryTransaction trans = new InventoryTransaction();
//        trans.setProduct(logBatch.getProduct());
//        trans.setBatch(logBatch);
//        trans.setType(InventoryTransactionType.IMPORT);
//        trans.setQuantity(logBatch.getQuantity());
//        trans.setCreatedAt(LocalDateTime.now());
//
//        inventoryTransactionRepository.save(trans);
//    }

    /**
     * This method is scheduled to run automatically to check for expired batches.
     * It finds batches that are expired and not yet marked as WAITING_TO_CANCLE or EXPIRED,
     * and updates their status to WAITING_TO_CANCLE.
     */
    @Scheduled(cron = "0 0 1 * * ?") // Runs every day at 1:00 AM
    @Transactional
    public void updateExpiredBatches() {
        LocalDate today = LocalDate.now();

        // Find batches that are expired (expiry_date < today) and have a status that can be changed
        // (e.g., DONE, PROCESSING). We don't want to change batches that are already handled.
        List<LogBatch> expiredBatches = logBatchRepository.findByExpiryDateBeforeAndStatusIn(
                today,
                List.of(LogBatchStatus.DONE, LogBatchStatus.PROCESSING)
        );

        for (LogBatch batch : expiredBatches) {
            updateLogBatchStatus(batch.getBatchId(), LogBatchStatus.WAITING_TO_CANCLE);
            logBatchRepository.save(batch);
            // You might want to add logging here to record which batches were updated
            System.out.println("Batch ID " + batch.getBatchId() + " has expired and status updated to WAITING_TO_CANCLE.");
        }
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
