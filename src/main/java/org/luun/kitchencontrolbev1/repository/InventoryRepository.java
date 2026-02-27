package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import java.time.LocalDate;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByBatchBatchId(Integer batchId);

    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.product.productId = :productId")
    Float sumQuantityByProductId(@Param("productId") Integer productId);

    List<Inventory> findByProductProductIdAndQuantityGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
            Integer productId, Float quantity, LocalDate date);
}
