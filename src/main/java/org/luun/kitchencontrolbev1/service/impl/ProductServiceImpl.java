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

import org.luun.kitchencontrolbev1.repository.InventoryRepository;
import org.luun.kitchencontrolbev1.repository.OrderDetailRepository;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public List<ProductResponse> getProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductByType(String productType) {
        // Assuming findByProductType exists in repository or needs to be added
        // For now, let's assume it returns a single Product or null
        // If it returns a List, this logic needs adjustment
        List<Product> products = productRepository.findByProductType(productType);
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
        if (product == null)
            return null;
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setProductName(product.getProductName());

        if (product.getProductType() != null) {
            response.setProductType(product.getProductType().toString());
        }
        response.setUnit(product.getUnit());
        response.setShelfLifeDays(product.getShelfLifeDays());

        // FLOW 1 - Bước 1: Tính toán Tồn kho khả dụng (Available Stock)
        // Hệ thống sẽ lấy tổng Inventory thực tế chưa trừ
        Float totalInventory = inventoryRepository.sumQuantityByProductId(product.getProductId());
        if (totalInventory == null)
            totalInventory = 0f;

        // Trừ đi tổng số lượng (quantity) của các order đang ở trạng thái WAITTING và
        // PROCESSING (Gom đơn & phân bổ)
        Float pendingOrders = orderDetailRepository.sumQuantityByProductIdAndOrderStatuses(
                product.getProductId(),
                Arrays.asList(OrderStatus.WAITTING, OrderStatus.PROCESSING));
        if (pendingOrders == null)
            pendingOrders = 0f;

        // Available = Tổng Inventory - Tổng quantity các đơn đang chờ
        response.setAvailableStock(totalInventory - pendingOrders);
        return response;
    }
}
