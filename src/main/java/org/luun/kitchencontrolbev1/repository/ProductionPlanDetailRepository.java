package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.ProductionPlanDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductionPlanDetailRepository extends JpaRepository<ProductionPlanDetail, Integer> {
    List<ProductionPlanDetail> findByProductionPlan_PlanId(Integer planId);
}
