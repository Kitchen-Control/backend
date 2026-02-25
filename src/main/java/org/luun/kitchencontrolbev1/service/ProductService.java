package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.ProductRequest;
import org.luun.kitchencontrolbev1.dto.response.ProductResponse;
import org.luun.kitchencontrolbev1.entity.Product;

import java.util.List;

public interface ProductService {
    List<ProductResponse> getProducts();
    List<ProductResponse> getProductByType(String productType);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Integer productId, Product updatedProduct);
    List<ProductResponse> searchProductByProductName(String keyword);
}
