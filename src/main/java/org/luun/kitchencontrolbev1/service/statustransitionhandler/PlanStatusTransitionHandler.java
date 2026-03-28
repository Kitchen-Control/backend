package org.luun.kitchencontrolbev1.service.statustransitionhandler;

import org.luun.kitchencontrolbev1.entity.ProductionPlan;
import org.luun.kitchencontrolbev1.enums.ProductionPlanStatus;
import org.luun.kitchencontrolbev1.service.handler.productionplan.PlanStatusHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PlanStatusTransitionHandler {
    private final Map<ProductionPlanStatus, PlanStatusHandler> handlers;

    public PlanStatusTransitionHandler(List<PlanStatusHandler> handlerList) {
        this.handlers = handlerList
                .stream()
                .collect(Collectors.toMap(
                        PlanStatusHandler::supportedStatus,
                        h -> h
                ));
    }

    public void handle(ProductionPlan plan, ProductionPlanStatus newStatus) {

        PlanStatusHandler handler = handlers.get(newStatus);

        if (handler != null) {
            handler.handle(plan);
        }
    }

}
