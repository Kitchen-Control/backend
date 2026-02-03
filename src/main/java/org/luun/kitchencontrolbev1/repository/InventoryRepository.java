package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
}
