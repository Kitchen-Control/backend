package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByOrder_OrderId(Integer orderId);
}
