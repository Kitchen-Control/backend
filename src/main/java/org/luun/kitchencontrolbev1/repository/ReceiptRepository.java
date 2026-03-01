package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Integer> {
    List<Receipt> findByOrder_OrderId(Integer orderId);
}
