package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    // This is the explicit and correct way to traverse nested properties.
    // It tells Spring Data JPA to look for the 'store' property in Order,
    // and then the 'storeId' property within that Store entity.
    List<Order> findByStore_StoreId(Integer storeId);
}
