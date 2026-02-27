package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.OrderDetailFill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDetailFillRepository extends JpaRepository<OrderDetailFill, Integer> {
    @Query("SELECT SUM(odf.quantity) FROM OrderDetailFill odf WHERE odf.batch.batchId = :batchId AND odf.orderDetail.order.status IN :statuses")
    Float sumQuantityByBatchIdAndOrderStatuses(
            @Param("batchId") Integer batchId,
            @Param("statuses") List<OrderStatus> statuses);
}
