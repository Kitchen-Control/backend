package org.luun.kitchencontrolbev1.service.impl;

import org.luun.kitchencontrolbev1.entity.*;
import org.luun.kitchencontrolbev1.enums.*;
import org.luun.kitchencontrolbev1.repository.*;
import org.luun.kitchencontrolbev1.dto.response.ReceiptResponse;
import org.luun.kitchencontrolbev1.service.ReceiptService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final OrderRepository orderRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public ReceiptResponse createReceiptForOrder(Integer orderId, String note) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        Receipt receipt = new Receipt();
        receipt.setReceiptCode("REC-" + UUID.randomUUID().toString().substring(0, 8));
        receipt.setOrder(order);
        receipt.setStatus("DRAFT");
        receipt.setNote(note);
        receipt.setExportDate(LocalDateTime.now());

        Receipt saved = receiptRepository.save(receipt);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ReceiptResponse completeReceipt(Integer receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));
        if (!"DRAFT".equals(receipt.getStatus())) {
            throw new RuntimeException("Receipt is not DRAFT");
        }
        receipt.setStatus("COMPLETED");

        Order order = receipt.getOrder();
        if (order != null && order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                if (detail.getOrderDetailFills() != null) {
                    for (OrderDetailFill fill : detail.getOrderDetailFills()) {
                        // Create InventoryTransaction
                        InventoryTransaction txn = new InventoryTransaction();
                        txn.setProduct(detail.getProduct());
                        txn.setBatch(fill.getBatch());
                        txn.setType(InventoryTransactionType.EXPORT);
                        txn.setQuantity(fill.getQuantity());
                        txn.setCreatedAt(LocalDateTime.now());
                        txn.setNote("Export for receipt " + receipt.getReceiptCode());
                        txn.setReceipt(receipt);
                        inventoryTransactionRepository.save(txn);

                        // Deduct from Inventory
                        Inventory inventory = inventoryRepository.findByBatchBatchId(fill.getBatch().getBatchId())
                                .orElseThrow(() -> new RuntimeException("Inventory not found for batch"));
                        inventory.setQuantity(inventory.getQuantity() - fill.getQuantity());
                        inventoryRepository.save(inventory);
                    }
                }
            }
        }

        Receipt saved = receiptRepository.save(receipt);
        return mapToResponse(saved);
    }

    @Override
    public List<ReceiptResponse> getReceiptsByOrderId(Integer orderId) {
        return receiptRepository.findByOrderOrderId(orderId).stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReceiptResponse mapToResponse(Receipt receipt) {
        ReceiptResponse response = new ReceiptResponse();
        response.setReceiptId(receipt.getReceiptId());
        response.setReceiptCode(receipt.getReceiptCode());
        if (receipt.getOrder() != null) {
            response.setOrderId(receipt.getOrder().getOrderId());
        }
        response.setExportDate(receipt.getExportDate());
        response.setStatus(receipt.getStatus());
        response.setNote(receipt.getNote());
        return response;
    }
}
