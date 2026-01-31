package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.ProductResponse;
import org.luun.kitchencontrolbev1.entity.Product;
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
    public ProductResponse getProductByType(String productType) {
        // Assuming findByProductType exists in repository or needs to be added
        // For now, let's assume it returns a single Product or null
        // If it returns a List, this logic needs adjustment
        // Product product = productRepository.findByProductType(productType);
        // return mapToResponse(product);
        return null; // Placeholder until repository is updated
    }

    @Override
    public ProductResponse createProduct(Product product) {
        Product newProduct = productRepository.save(product);
        return mapToResponse(newProduct);
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
        // Assuming we want to return the first matched product
        if (products.isEmpty()) {
            return null;
        }

        List<ProductResponse> responses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return responses;
    }

    private ProductResponse mapToResponse(Product product) {
        if (product == null) return null;
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setProductName(product.getProductName());

        // Check if productType is not null before toString()
        if (product.getProductType() != null) {
            response.setProductType(product.getProductType().toString());
        }
        response.setUnit(product.getUnit());
        response.setShelfLifeDays(product.getShelfLifeDays());
        return response;
    }
}
