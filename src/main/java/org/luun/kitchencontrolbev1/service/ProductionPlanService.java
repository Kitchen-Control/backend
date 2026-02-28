package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.ProductionPlanRequest;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanResponse;

import java.util.List;

public interface ProductionPlanService {
    List<ProductionPlanResponse> getProductionPlans();
    ProductionPlanResponse getProductionPlanById(Integer id);
    ProductionPlanResponse createProductionPlan(ProductionPlanRequest request);
}
