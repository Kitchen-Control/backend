package org.luun.kitchencontrolbev1.service.handler.productionplan;

import org.luun.kitchencontrolbev1.entity.ProductionPlan;
import org.luun.kitchencontrolbev1.enums.ProductionPlanStatus;
import org.springframework.stereotype.Component;

@Component
public interface PlanStatusHandler {
    ProductionPlanStatus supportedStatus();
    void handle(ProductionPlan plan);
}
