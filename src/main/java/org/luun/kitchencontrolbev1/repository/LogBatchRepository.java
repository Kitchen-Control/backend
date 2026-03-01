package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogBatchRepository extends JpaRepository<LogBatch, Integer> {
    List<LogBatch> findByPlan_PlanId(Integer planId);

    List<LogBatch> findByProduct_ProductId(Integer productId);
}
