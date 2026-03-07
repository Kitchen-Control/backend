package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.LogBatchRequest;
import org.luun.kitchencontrolbev1.dto.response.LogBatchResponse;
import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.entity.ProductionPlan;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.enums.LogBatchType;
import org.luun.kitchencontrolbev1.repository.LogBatchRepository;
import org.luun.kitchencontrolbev1.repository.ProductRepository;
import org.luun.kitchencontrolbev1.repository.ProductionPlanRepository;
import org.luun.kitchencontrolbev1.service.LogBatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogBatchServiceImpl implements LogBatchService {

    private final LogBatchRepository logBatchRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ProductRepository productRepository;

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
    @Transactional
    public LogBatchResponse createLogBatch(LogBatchRequest request) {
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

        if (request.getType() == null) {
            throw new RuntimeException("Type is required");
        }

        logBatch.setType(request.getType());

        if (request.getType() == LogBatchType.PRODUCTION) {
            logBatch.setStatus(LogBatchStatus.PROCESSING);
        } else {
            logBatch.setStatus(LogBatchStatus.DONE);
        }

        logBatch.setCreatedAt(LocalDateTime.now());

        LogBatch savedBatch = logBatchRepository.save(logBatch);
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
