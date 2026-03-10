package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByOrder_OrderId(Integer orderId);

    //Dùng native query để tránh lỗi, native query là dùng trực tiếp tên bảng và cột trong database, còn JPQL là dùng tên entity để truy vấn
    @Query(value = "SELECT COALESCE(SUM(od.quantity), 0) " +
            "FROM order_details od " +
            "JOIN orders o ON o.order_id = od.order_id " +
            "WHERE od.product_id = :productId AND o.status::text IN (:statuses)",
            nativeQuery = true)
    Float getTotalQuantityByProductIdAndOrderStatusIn(@Param("productId") Integer productId,
                                                      @Param("statuses") List<String> statuses);
}
