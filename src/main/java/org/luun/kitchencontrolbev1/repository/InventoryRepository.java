package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByBatchBatchId(Integer batchId);

    @Query(value = "SELECT COALESCE(SUM(i.quantity), 0) FROM inventories i " +
            "JOIN log_batches lb ON lb.batch_id = i.batch_id " +
            "WHERE i.product_id = :productId " +
            "AND lb.status::text IN ('DONE', 'PROCESSING')", nativeQuery = true)
    Float getTotalQuantityByProductId(@Param("productId") Integer productId);

    // This query retrieves all inventory records for a specific product that have not expired
    // and have a quantity greater than 0, ordered by expiry date in ascending order.
    @Query("SELECT i " +
            "FROM Inventory i " +
            "WHERE i.product.productId = :productId AND i.expiryDate >= CURRENT_DATE AND i.quantity > 0 ORDER BY i.expiryDate ASC")
    List<Inventory> findValidInventoriesForProductOrderByExpiryDateAsc(@Param("productId") Integer productId);

    List<Inventory> findByProductOrderByExpiryDateAsc(Product product);
}
