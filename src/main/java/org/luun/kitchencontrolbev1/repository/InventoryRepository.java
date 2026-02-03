package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByBatchBatchId(Integer batchId);
}
