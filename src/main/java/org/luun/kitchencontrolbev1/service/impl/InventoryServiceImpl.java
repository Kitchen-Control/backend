package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.InventoryResponse;
import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.enums.ProductType;
import org.luun.kitchencontrolbev1.repository.InventoryRepository;
import org.luun.kitchencontrolbev1.repository.OrderDetailRepository;
import org.luun.kitchencontrolbev1.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public Float getAvailableStock(Integer productId) {
        Float totalInventory = inventoryRepository.getTotalQuantityByProductId(productId);
        if (totalInventory == null)
            totalInventory = 0f;

        List<OrderStatus> statuses = Arrays.asList(OrderStatus.WAITING, OrderStatus.PROCESSING);
        Float totalOrdered = orderDetailRepository.getTotalQuantityByProductIdAndOrderStatusIn(productId, statuses);
        if (totalOrdered == null)
            totalOrdered = 0f;

        return totalInventory - totalOrdered;
    }

    @Override
    public Inventory getInventoryByBatchId(Integer batchId) {
        return inventoryRepository.findByBatchBatchId(batchId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for batch: " + batchId));
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
    public List<InventoryResponse> getInventoryByProductType(ProductType productType) {
        List<Inventory> inventories = inventoryRepository.findByProduct_ProductTypeOrderByExpiryDateAsc(productType);
        return inventories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Inventory createInventoryFromBatch(LogBatch batch) {

        if (inventoryRepository.findByBatchBatchId(batch.getBatchId()).isPresent()) {
            throw new RuntimeException("Inventory already exists for this batch");
        }

        Inventory inventory = new Inventory();
        inventory.setProduct(batch.getProduct());
        inventory.setBatch(batch);
        inventory.setQuantity(batch.getQuantity());
        inventory.setExpiryDate(batch.getExpiryDate());

        return inventoryRepository.save(inventory);
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
