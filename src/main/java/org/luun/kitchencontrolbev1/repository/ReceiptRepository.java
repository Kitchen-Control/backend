package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, Integer> {
}
