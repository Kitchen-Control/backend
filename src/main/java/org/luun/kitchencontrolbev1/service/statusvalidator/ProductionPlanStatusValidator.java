package org.luun.kitchencontrolbev1.service.statusvalidator;

import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.enums.ProductionPlanStatus;

import java.util.Map;
import java.util.Set;

public class ProductionPlanStatusValidator {

    private static final Map<ProductionPlanStatus, Set<ProductionPlanStatus>> TRANSITIONS = Map.of(

    );

//    public void validate(LogBatchStatus current, LogBatchStatus newStatus) {
//        Set<LogBatchStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());
//
//        if(!allowed.contains(newStatus)) {
//            throw new IllegalStateException("Invalid transition from " + current + " to " + newStatus);
//        }
//    }
}
