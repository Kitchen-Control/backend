package org.luun.kitchencontrolbev1.service.statusvalidator;

import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class LogBatchStatusValidator {

    private static final Map<LogBatchStatus, Set<LogBatchStatus>> TRANSITIONS = Map.of(
            LogBatchStatus.PROCESSING, Set.of(LogBatchStatus.WAITING_TO_CONFIRM, LogBatchStatus.WAITING_TO_CANCEL),
            LogBatchStatus.WAITING_TO_CONFIRM, Set.of(LogBatchStatus.DONE),
            LogBatchStatus.DONE, Set.of(LogBatchStatus.WAITING_TO_CANCEL),
            LogBatchStatus.WAITING_TO_CANCEL, Set.of(LogBatchStatus.DAMAGED),
            LogBatchStatus.DAMAGED, Set.of()
    );

    public void validate(LogBatchStatus current, LogBatchStatus newStatus) {
        Set<LogBatchStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());

        if(!allowed.contains(newStatus)) {
            throw new IllegalStateException("Invalid transition from " + current + " to " + newStatus);
        }
    }
}
