package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.dto.response.ProductResponse;
import org.luun.kitchencontrolbev1.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByProductType(String productType);
    List<Product> searchProductByProductNameContainingIgnoreCase(String keyword);
}
