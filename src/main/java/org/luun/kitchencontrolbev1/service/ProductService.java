package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.ProductRequest;
import org.luun.kitchencontrolbev1.dto.response.ProductResponse;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.enums.ProductType;

import java.util.List;

public interface ProductService {
    List<ProductResponse> getProducts();
    List<ProductResponse> getProductByType(ProductType productType);
    Product getProductById(Integer productId);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Integer productId, Product updatedProduct);
    List<ProductResponse> searchProductByProductName(String keyword);
}
