package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.ProductionPlanRequest;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanResponse;
import org.luun.kitchencontrolbev1.entity.ProductionPlan;

import java.util.List;

public interface ProductionPlanService {
    List<ProductionPlanResponse> getProductionPlans();
    ProductionPlanResponse getProductionPlanById(Integer id);
    ProductionPlan getProductionPlanEntityById(Integer id);
    ProductionPlanResponse createProductionPlan(ProductionPlanRequest request);
    ProductionPlanResponse updateProductionPlan(Integer id, ProductionPlanRequest request);
}
