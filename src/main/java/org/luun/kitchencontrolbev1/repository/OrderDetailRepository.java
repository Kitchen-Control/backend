package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
}
