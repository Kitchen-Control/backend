package org.luun.hsf.asm01.kitchencontrolbe.repository;

import org.luun.hsf.asm01.kitchencontrolbe.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
}
