package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.LogBatchRequest;
import org.luun.kitchencontrolbev1.dto.response.LogBatchResponse;
import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;

import java.util.List;

public interface LogBatchService {
    List<LogBatchResponse> getAllLogBatches();

    LogBatchResponse getLogBatchById(Integer batchId);

    LogBatch getLogBatchEntityById(Integer batchId);

    List<LogBatchResponse> getLogBatchesByPlanId(Integer planId);

    List<LogBatchResponse> getLogBatchesByProductId(Integer productId);

    List<LogBatchResponse> getLogBatchesByStatus(LogBatchStatus status);

    void createProductionLogBatch(List<LogBatchRequest> requests);

    LogBatchResponse createPurchaseLogBatch(LogBatchRequest request);

    void updateLogBatchStatus(Integer batchId, LogBatchStatus status);
}
