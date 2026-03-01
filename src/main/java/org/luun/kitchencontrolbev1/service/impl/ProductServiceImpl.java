package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.ProductRequest;
import org.luun.kitchencontrolbev1.dto.response.ProductResponse;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.enums.ProductType;
import org.luun.kitchencontrolbev1.repository.ProductRepository;
import org.luun.kitchencontrolbev1.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public List<ProductResponse> getProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductByType(String productType) {
        // Convert the incoming String to the ProductType enum
        ProductType type;
        try {
            type = ProductType.valueOf(productType.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle cases where the string is not a valid enum constant
            throw new RuntimeException("Invalid product type: " + productType);
        }

        List<Product> products = productRepository.findByProductType(type);
         return products.stream()
                 .map(this::mapToResponse)
                 .collect(Collectors.toList());
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setProductType(ProductType.valueOf(request.getProductType()));
        product.setUnit(request.getUnit());
        product.setShelfLifeDays(request.getShelfLifeDay());
        
        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    @Override
    public ProductResponse updateProduct(Integer productId, Product updatedProduct) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id " + productId));
        
        product.setProductName(updatedProduct.getProductName());
        product.setProductType(updatedProduct.getProductType());
        product.setUnit(updatedProduct.getUnit());
        product.setShelfLifeDays(updatedProduct.getShelfLifeDays());
        
        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    @Override
    public List<ProductResponse> searchProductByProductName(String keyword) {
        List<Product> products = productRepository.searchProductByProductNameContainingIgnoreCase(keyword);
        if (products.isEmpty()) {
            return null;
        }

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToResponse(Product product) {
        if (product == null) return null;
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setProductName(product.getProductName());

        if (product.getProductType() != null) {
            response.setProductType(product.getProductType().toString());
        }
        response.setUnit(product.getUnit());
        response.setShelfLifeDays(product.getShelfLifeDays());
        return response;
    }
}
