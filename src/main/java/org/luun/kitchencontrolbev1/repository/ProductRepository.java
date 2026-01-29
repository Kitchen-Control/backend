package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Product findByProductType(String productType);
}
