package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {
    List<InventoryTransaction> findByProductProductId(Integer productId);
    List<InventoryTransaction> findByBatchBatchId(Integer batchId);
}
