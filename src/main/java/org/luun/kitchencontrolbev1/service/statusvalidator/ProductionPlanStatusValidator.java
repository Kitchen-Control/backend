package org.luun.kitchencontrolbev1.service.statusvalidator;

import org.luun.kitchencontrolbev1.enums.ProductionPlanStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ProductionPlanStatusValidator {

    private static final Map<ProductionPlanStatus, Set<ProductionPlanStatus>> TRANSITIONS = Map.of(
            ProductionPlanStatus.DRAFT, Set.of(ProductionPlanStatus.WAITING, ProductionPlanStatus.CANCEL),
            ProductionPlanStatus.WAITING, Set.of(ProductionPlanStatus.PROCESSING),
            ProductionPlanStatus.PROCESSING, Set.of(ProductionPlanStatus.COMPLETE_ONE_SECTION, ProductionPlanStatus.DONE),
            ProductionPlanStatus.COMPLETE_ONE_SECTION, Set.of(),
            ProductionPlanStatus.DONE, Set.of(),
            ProductionPlanStatus.CANCEL, Set.of()
    );

    public void validate(ProductionPlanStatus current, ProductionPlanStatus newStatus) {

        Set<ProductionPlanStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());

        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException("Invalid transition from " + current + " to " + newStatus);
        }
    }
}
