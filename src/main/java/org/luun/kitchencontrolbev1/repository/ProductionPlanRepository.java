package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.ProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Integer> {
}
