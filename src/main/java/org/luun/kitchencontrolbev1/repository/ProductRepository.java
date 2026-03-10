package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    // The parameter type must match the entity's attribute type for direct comparison.
    @Query(value = "SELECT * FROM products p WHERE p.product_type::text = :productType", nativeQuery = true)
    List<Product> findByProductType(@Param("productType") String productType);
    List<Product> searchProductByProductNameContainingIgnoreCase(String keyword);
}
