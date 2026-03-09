package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.InventoryResponse;
import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.entity.InventoryTransaction;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.repository.InventoryRepository;
import org.luun.kitchencontrolbev1.repository.InventoryTransactionRepository;
import org.luun.kitchencontrolbev1.repository.OrderDetailRepository;
import org.luun.kitchencontrolbev1.repository.ProductRepository;
import org.luun.kitchencontrolbev1.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    public Float getAvailableStock(Integer productId) {
        Float totalInventory = inventoryRepository.getTotalQuantityByProductId(productId);
        if (totalInventory == null)
            totalInventory = 0f;

        List<OrderStatus> statuses = Arrays.asList(OrderStatus.WAITTING, OrderStatus.PROCESSING);
        Float totalOrdered = orderDetailRepository.getTotalQuantityByProductIdAndOrderStatusIn(productId, statuses);
        if (totalOrdered == null)
            totalOrdered = 0f;

        return totalInventory - totalOrdered;
    }

    @Override
    public List<InventoryResponse> getInventories() {
        List<Inventory> inventories = inventoryRepository.findAll();
        return inventories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryResponse getInventoryById(Integer inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
        return mapToResponse(inventory);
    }

    @Override
    @Transactional
    public void decreaseProductQuantity(Integer productId, float quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        List<Inventory> inventories = inventoryRepository.findByProductOrderByExpiryDateAsc(product);

        float remainingQuantityToDecrease = quantity;

        for (Inventory inventory : inventories) {
            if (remainingQuantityToDecrease <= 0) {
                break;
            }

            float currentInventoryQuantity = inventory.getQuantity();
            float quantityToDecrease = Math.min(remainingQuantityToDecrease, currentInventoryQuantity);

            inventory.setQuantity(currentInventoryQuantity - quantityToDecrease);
            inventoryRepository.save(inventory);

            remainingQuantityToDecrease -= quantityToDecrease;

            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(product);
            transaction.setBatch(inventory.getBatch());
            transaction.setType(InventoryTransactionType.EXPORT);
            transaction.setQuantity(quantityToDecrease);
            transaction.setCreatedAt(LocalDateTime.now());
            inventoryTransactionRepository.save(transaction);
        }

        if (remainingQuantityToDecrease > 0) {
            throw new RuntimeException("Not enough stock for product with id: " + productId);
        }
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setInventoryId(inventory.getInventoryId());
        response.setProduct_name(inventory.getProduct().getProductName());
        response.setBatch(inventory.getBatch());
        response.setQuantity(inventory.getQuantity());
        response.setExpiryDate(inventory.getExpiryDate());
        return response;
    }
}
