package org.luun.kitchencontrolbev1.service.statustransitionhandler;

import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.service.handler.logbatch.LogBatchStatusHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LogBatchStatusTransitionHandler {

    private final Map<LogBatchStatus, LogBatchStatusHandler> handlers;

    public LogBatchStatusTransitionHandler(List<LogBatchStatusHandler> handlerList) {
        this.handlers = handlerList
                .stream()
                .collect(Collectors.toMap(
                        LogBatchStatusHandler::supportedStatus,
                        h -> h));
    }

    public void handle(LogBatch batch, LogBatchStatus newStatus) {

        LogBatchStatusHandler handler = handlers.get(newStatus);

        if (handler != null) {
            handler.handle(batch);
        }
    }
}
