package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.entity.ProductionPlan;

import java.util.List;

public interface ProductionPlanService {
    List<ProductionPlan> getProductionPlans();
    ProductionPlan getProductionPlanById(Integer id);
    ProductionPlan createProductionPlan(ProductionPlan productionPlan);
//    ProductionPlan updateProductionPlan(Integer id, ProductionPlan updatedProductionPlan);
//    void deleteProductionPlan(Integer id);
}
