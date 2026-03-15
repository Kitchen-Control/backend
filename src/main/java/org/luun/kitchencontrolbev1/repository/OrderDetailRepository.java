package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByOrder_OrderId(Integer orderId);

    @Query("""
            SELECT COALESCE(SUM(od.quantity), 0) 
            FROM OrderDetail od JOIN od.order o 
            WHERE od.product.productId = :productId AND o.status IN :statuses
            """)
    Float getTotalQuantityByProductIdAndOrderStatusIn(@Param("productId") Integer productId,
            @Param("statuses") List<OrderStatus> statuses);
}
