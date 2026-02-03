package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.InventoryTransactionRequest;
import org.luun.kitchencontrolbev1.dto.response.InventoryTransactionResponse;
import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.entity.InventoryTransaction;
import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import org.luun.kitchencontrolbev1.repository.InventoryRepository;
import org.luun.kitchencontrolbev1.repository.InventoryTransactionRepository;
import org.luun.kitchencontrolbev1.repository.LogBatchRepository;
import org.luun.kitchencontrolbev1.repository.ProductRepository;
import org.luun.kitchencontrolbev1.service.InventoryTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    private final InventoryTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final LogBatchRepository logBatchRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public List<InventoryTransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryTransactionResponse> getTransactionsByProductId(Integer productId) {
        return transactionRepository.findByProductProductId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryTransactionResponse> getTransactionsByBatchId(Integer batchId) {
        return transactionRepository.findByBatchBatchId(batchId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryTransactionResponse createTransaction(InventoryTransactionRequest request) {
        // 1. Validate Product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        // 2. Validate Batch (Optional, but recommended if tracking by batch)
        LogBatch batch = null;
        if (request.getBatchId() != null) {
            batch = logBatchRepository.findById(request.getBatchId())
                    .orElseThrow(() -> new RuntimeException("Batch not found with id: " + request.getBatchId()));
            
            // Ensure batch belongs to the product
            if (!batch.getProduct().getProductId().equals(product.getProductId())) {
                throw new RuntimeException("Batch does not belong to the specified product");
            }
        }

        // 3. Create Transaction
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setBatch(batch);
        transaction.setType(request.getType());
        transaction.setQuantity(request.getQuantity());
        transaction.setNote(request.getNote());
        transaction.setCreatedAt(LocalDateTime.now());

        // 4. Update Inventory
        updateInventory(product, batch, request.getType(), request.getQuantity());

        InventoryTransaction savedTransaction = transactionRepository.save(transaction);
        return mapToResponse(savedTransaction);
    }

    private void updateInventory(Product product, LogBatch batch, InventoryTransactionType type, Float quantity) {
        // Find inventory by batch if batch is present, otherwise find by product (or create new logic)
        // Assuming inventory is tracked by batch based on Inventory entity structure
        
        if (batch == null) {
             // If no batch is specified, we might need a default inventory or throw error depending on business logic.
             // For now, let's assume we need a batch for inventory tracking as per Inventory entity (OneToOne with Batch)
             throw new RuntimeException("Batch ID is required for inventory update");
        }

        Inventory inventory = inventoryRepository.findByBatchBatchId(batch.getBatchId())
                .orElse(null);

        if (inventory == null) {
            if (type == InventoryTransactionType.EXPORT) {
                throw new RuntimeException("Cannot export from non-existent inventory");
            }
            // Create new inventory record for IMPORT
            inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setBatch(batch);
            inventory.setQuantity(0f);
            inventory.setExpiryDate(batch.getExpiryDate());
        }

        if (type == InventoryTransactionType.IMPORT) {
            inventory.setQuantity(inventory.getQuantity() + quantity);
        } else {
            if (inventory.getQuantity() < quantity) {
                throw new RuntimeException("Insufficient inventory quantity");
            }
            inventory.setQuantity(inventory.getQuantity() - quantity);
        }

        inventoryRepository.save(inventory);
    }

    private InventoryTransactionResponse mapToResponse(InventoryTransaction transaction) {
        InventoryTransactionResponse response = new InventoryTransactionResponse();
        response.setTransactionId(transaction.getTransactionId());
        
        if (transaction.getProduct() != null) {
            response.setProductId(transaction.getProduct().getProductId());
            response.setProductName(transaction.getProduct().getProductName());
        }
        
        if (transaction.getBatch() != null) {
            response.setBatchId(transaction.getBatch().getBatchId());
        }
        
        response.setType(transaction.getType());
        response.setQuantity(transaction.getQuantity());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setNote(transaction.getNote());
        
        return response;
    }
}
