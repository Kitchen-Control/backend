package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    // Custom query methods if needed
    List<Order> findByStoreStoreId(Integer storeId);
}
