package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.ProductResponse;
import org.luun.kitchencontrolbev1.entity.Product;

import java.util.List;

public interface ProductService {
    List<ProductResponse> getProducts();
    ProductResponse getProductByType(String productType);
    ProductResponse createProduct(Product product);
    ProductResponse updateProduct(Integer productId, Product updatedProduct);
}
