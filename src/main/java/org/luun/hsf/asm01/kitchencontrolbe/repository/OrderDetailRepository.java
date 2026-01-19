package org.luun.hsf.asm01.kitchencontrolbe.repository;

import org.luun.hsf.asm01.kitchencontrolbe.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
}
