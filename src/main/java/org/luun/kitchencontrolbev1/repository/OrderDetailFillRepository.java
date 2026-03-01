package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.OrderDetailFill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailFillRepository extends JpaRepository<OrderDetailFill, Integer> {
    List<OrderDetailFill> findByOrderDetail_OrderDetailId(Integer orderDetailId);

    List<OrderDetailFill> findByBatch_BatchId(Integer batchId);
}
