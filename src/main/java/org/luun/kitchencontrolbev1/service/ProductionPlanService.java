package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.ProductionPlanResponse;
import org.luun.kitchencontrolbev1.entity.ProductionPlan;

import java.util.List;

public interface ProductionPlanService {
    List<ProductionPlanResponse> getProductionPlans();
    ProductionPlanResponse getProductionPlanById(Integer id);
    ProductionPlanResponse createProductionPlan(ProductionPlan productionPlan);
//    ProductionPlan updateProductionPlan(Integer id, ProductionPlan updatedProductionPlan);
//    void deleteProductionPlan(Integer id);
}
