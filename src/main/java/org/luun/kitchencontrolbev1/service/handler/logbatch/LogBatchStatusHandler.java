package org.luun.kitchencontrolbev1.service.handler.logbatch;

import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.springframework.stereotype.Component;

@Component
public interface LogBatchStatusHandler {

    LogBatchStatus supportedStatus();

    void handle(LogBatch batch);
}
