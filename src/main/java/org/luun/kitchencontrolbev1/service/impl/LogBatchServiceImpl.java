package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.luun.kitchencontrolbev1.dto.request.LogBatchRequest;
import org.luun.kitchencontrolbev1.dto.response.LogBatchResponse;
import org.luun.kitchencontrolbev1.entity.*;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.enums.LogBatchType;
import org.luun.kitchencontrolbev1.repository.LogBatchRepository;
import org.luun.kitchencontrolbev1.repository.ProductRepository;
import org.luun.kitchencontrolbev1.repository.ProductionPlanRepository;
import org.luun.kitchencontrolbev1.service.InventoryService;
import org.luun.kitchencontrolbev1.service.LogBatchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogBatchServiceImpl implements LogBatchService {

    private final LogBatchRepository logBatchRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

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
        logBatch.setExpiryDate(request.getExpiryDate());
        logBatch.setCreatedAt(LocalDateTime.now());
        logBatch.setStatus(LogBatchStatus.PROCESSING);

        if (request.getType() == null || request.getType() == LogBatchType.PRODUCTION) {
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
        logBatch.setStatus(LogBatchStatus.PROCESSING);

        if (request.getType() == null || request.getType() == LogBatchType.PURCHASE) {
            throw new RuntimeException("Type is required or must be PURCHASE");
        }

        logBatch.setType(request.getType());
        LogBatch savedBatch = logBatchRepository.save(logBatch);

        // Inventory
        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setQuantity(request.getQuantity());
        inv.setBatch(savedBatch);
        inv.setExpiryDate(savedBatch.getExpiryDate());

        // Inventory transaction
        InventoryTransaction inventoryTransaction = new InventoryTransaction();
        inventoryTransaction.setProduct(product);
        inventoryTransaction.setBatch(savedBatch);
        inventoryTransaction.setType(InventoryTransactionType.IMPORT);
        inventoryTransaction.setQuantity(request.getQuantity());
        inventoryTransaction.setCreatedAt(LocalDateTime.now());

        savedBatch.setInventory(inv);
        savedBatch.getInventoryTransactions().add(inventoryTransaction);

        return mapToResponse(savedBatch);
    }

    @Override
    @Transactional
    public LogBatchResponse updateLogBatchStatus(Integer batchId, LogBatchStatus status) {
        LogBatch logBatch = logBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("LogBatch not found with id: " + batchId));

        logBatch.setStatus(status);
        LogBatch updatedBatch = logBatchRepository.save(logBatch);
        return mapToResponse(updatedBatch);
    }

    @Override
    @Transactional
    public void expireLogBatch(Integer batchId) {
        LogBatch logBatch = logBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("LogBatch not found with id: " + batchId));

        if (logBatch.getStatus() != LogBatchStatus.WAITING_TO_CANCLE) {
            throw new RuntimeException("Only batches with status WAITING_TO_CANCLE can be expired.");
        }

        logBatch.setStatus(LogBatchStatus.EXPIRED);
        logBatchRepository.save(logBatch);

        inventoryService.decreaseProductQuantity(logBatch.getProduct().getProductId(), logBatch.getQuantity());
    }

    /**
     * This method is scheduled to run automatically to check for expired batches.
     * It finds batches that are expired and not yet marked as WAITING_TO_CANCLE or EXPIRED,
     * and updates their status to WAITING_TO_CANCLE.
     */
    @Scheduled(cron = "0 0 1 * * ?", zone = "Asia/Ho_Chi_Minh") // Runs every day at 1:00 AM and in Vietnam time
    @Transactional
    public void updateExpiredBatches() {
        log.info("Checking for expired batches...");
        LocalDate today = LocalDate.now();

        // Find batches that are expired (expiry_date < today) and have a status that can be changed
        // (e.g., DONE, PROCESSING). We don't want to change batches that are already handled.
        List<LogBatch> expiredBatches = logBatchRepository.findByExpiryDateBeforeAndStatusIn(
                today,
                List.of(LogBatchStatus.DONE, LogBatchStatus.PROCESSING)
        );

        for (LogBatch batch : expiredBatches) {
            batch.setStatus(LogBatchStatus.WAITING_TO_CANCLE);
            logBatchRepository.save(batch);
            // You might want to add logging here to record which batches were updated
            log.info("Batch ID " + batch.getBatchId() + " has expired and status updated to WAITING_TO_CANCLE.");
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
