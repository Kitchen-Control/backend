package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.LogBatchResponse;
import java.util.List;

public interface LogBatchService {
    List<LogBatchResponse> getAllLogBatches();

    LogBatchResponse getLogBatchById(Integer batchId);

    List<LogBatchResponse> getLogBatchesByPlanId(Integer planId);

    List<LogBatchResponse> getLogBatchesByProductId(Integer productId);
}
