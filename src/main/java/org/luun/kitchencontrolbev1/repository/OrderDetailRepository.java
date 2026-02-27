package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.luun.kitchencontrolbev1.enums.OrderStatus;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    @Query("SELECT SUM(od.quantity) FROM OrderDetail od WHERE od.product.productId = :productId AND od.order.status IN :statuses")
    Float sumQuantityByProductIdAndOrderStatuses(
            @Param("productId") Integer productId,
            @Param("statuses") List<OrderStatus> statuses);
}
