package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.InventoryTransactionResponse;
import org.luun.kitchencontrolbev1.dto.response.ReceiptResponse;
import org.luun.kitchencontrolbev1.entity.Receipt;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.entity.InventoryTransaction;
import org.luun.kitchencontrolbev1.enums.ReceiptStatus;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.repository.ReceiptRepository;
import org.luun.kitchencontrolbev1.service.OrderService;
import org.luun.kitchencontrolbev1.service.ReceiptService;
import org.luun.kitchencontrolbev1.service.statustransitionhandler.ReceiptStatusTransitionHandler;
import org.luun.kitchencontrolbev1.service.statusvalidator.ReceiptStatusValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final OrderService orderService;

    private final ReceiptRepository receiptRepository;
    private final ReceiptStatusValidator receiptStatusValidation;
    private final ReceiptStatusTransitionHandler receiptStatusTransitionHandler;

    @Override
    public List<ReceiptResponse> getByOrderId(Integer orderId) {
        List<Receipt> receipts = receiptRepository.findByOrder_OrderId(orderId);

        return receipts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReceiptResponse> getByStatus(ReceiptStatus status) {
        List<Receipt> receipts = receiptRepository.findByStatus(status);
        return receipts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    // Giai đoạn 3.2: Tạo Phiếu Xuất (Receipt Creation)
    // Thủ kho kiểm tra rồi ấn tạo phiếu
    public ReceiptResponse createReceipt(Integer orderId, String note) {
        Order order = orderService.getOrderById(orderId);

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new RuntimeException("Order status need to be PROCESSING");
        }

        Receipt receipt = new Receipt();
        receipt.setReceiptCode("REC-" + System.currentTimeMillis());
        receipt.setOrder(order);
        receipt.setStatus(ReceiptStatus.DRAFT);
        receipt.setExportDate(LocalDateTime.now());
        receipt.setNote(note);

        Receipt savedReceipt = receiptRepository.save(receipt);
        return mapToResponse(savedReceipt);
    }

    @Override
    @Transactional
    public void updateReceiptStatus(List<Integer> receiptIds, ReceiptStatus newStatus) {
        List<Receipt> receipts = receiptRepository.findAllById(receiptIds);

        if (receipts.size() != receiptIds.size()) {
            throw new RuntimeException("There is a receiptId which can not found");
        }

        for (Receipt receipt : receipts) {

            receiptStatusValidation.validate(receipt.getStatus(), newStatus);

            receiptStatusTransitionHandler.handle(receipt, newStatus);

            receipt.setStatus(newStatus);
        }
    }

    @Override
    @Transactional
    public void deleteReceipt(Integer receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Receipt not found with id: " + receiptId));

        if (receipt.getStatus() != ReceiptStatus.DRAFT) {
            throw new RuntimeException("Cannot delete a receipt that is not in DRAFT status.");
        }

        receiptRepository.delete(receipt);
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

        if (receipt.getInventoryTransactions() != null) {
            List<InventoryTransactionResponse> transactionResponses = receipt.getInventoryTransactions().stream()
                    .map(this::mapTransactionToResponse)
                    .collect(Collectors.toList());
            response.setInventoryTransactions(transactionResponses);
        } else {
            response.setInventoryTransactions(new ArrayList<>());
        }

        return response;
    }

    private InventoryTransactionResponse mapTransactionToResponse(InventoryTransaction transaction) {
        return InventoryTransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .productId(transaction.getProduct() != null ? transaction.getProduct().getProductId() : null)
                .productName(transaction.getProduct() != null ? transaction.getProduct().getProductName() : null)
                .batchId(transaction.getBatch() != null ? transaction.getBatch().getBatchId() : null)
                .type(transaction.getType())
                .quantity(transaction.getQuantity())
                .createdAt(transaction.getCreatedAt())
                .note(transaction.getNote())
                .build();
    }
}
