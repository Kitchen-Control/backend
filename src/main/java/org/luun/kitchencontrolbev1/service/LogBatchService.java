package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.LogBatchRequest;
import org.luun.kitchencontrolbev1.dto.response.LogBatchResponse;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;

import java.util.List;

public interface LogBatchService {
    List<LogBatchResponse> getAllLogBatches();

    LogBatchResponse getLogBatchById(Integer batchId);

    List<LogBatchResponse> getLogBatchesByPlanId(Integer planId);

    List<LogBatchResponse> getLogBatchesByProductId(Integer productId);

    LogBatchResponse createLogBatch(LogBatchRequest request);

    LogBatchResponse updateLogBatchStatus(Integer batchId, LogBatchStatus status);
}
