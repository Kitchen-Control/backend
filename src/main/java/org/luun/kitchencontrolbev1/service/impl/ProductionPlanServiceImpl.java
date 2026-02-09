package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.entity.ProductionPlan;
import org.luun.kitchencontrolbev1.repository.ProductionPlanRepository;
import org.luun.kitchencontrolbev1.service.ProductionPlanService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionPlanServiceImpl implements ProductionPlanService {

    private final ProductionPlanRepository productionPlanRepository;

    @Override
    public List<ProductionPlan> getProductionPlans() {
        return productionPlanRepository.findAll();
    }

    @Override
    public ProductionPlan getProductionPlanById(Integer id) {
        return productionPlanRepository.findById(id).orElse(null);
    }

    @Override
    public ProductionPlan createProductionPlan(ProductionPlan productionPlan) {
        return productionPlanRepository.save(productionPlan);
    }

}
