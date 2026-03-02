package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.OrderDetailFill;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailFillRepository extends JpaRepository<OrderDetailFill, Integer> {
        List<OrderDetailFill> findByOrderDetail_OrderDetailId(Integer orderDetailId);

        List<OrderDetailFill> findByBatch_BatchId(Integer batchId);

        List<OrderDetailFill> findByOrderDetail_Order_OrderId(Integer orderId);

        @Query("SELECT COALESCE(SUM(odf.quantity), 0) FROM OrderDetailFill odf " +
                        "JOIN odf.orderDetail od " +
                        "JOIN od.order o " +
                        "WHERE odf.batch.batchId = :batchId AND o.status IN :statuses")
        Float getTotalReservedQuantityByBatchId(@Param("batchId") Integer batchId,
                        @Param("statuses") List<OrderStatus> statuses);
}
