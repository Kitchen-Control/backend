package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.ReceiptResponse;
import org.luun.kitchencontrolbev1.entity.Receipt;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.entity.OrderDetailFill;
import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.entity.InventoryTransaction;
import org.luun.kitchencontrolbev1.enums.ReceiptStatus;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import org.luun.kitchencontrolbev1.repository.OrderRepository;
import org.luun.kitchencontrolbev1.repository.ReceiptRepository;
import org.luun.kitchencontrolbev1.repository.OrderDetailFillRepository;
import org.luun.kitchencontrolbev1.repository.InventoryRepository;
import org.luun.kitchencontrolbev1.repository.InventoryTransactionRepository;
import org.luun.kitchencontrolbev1.service.ReceiptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailFillRepository orderDetailFillRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    public List<ReceiptResponse> getByOrderId(Integer orderId) {
        List<Receipt> receipts = receiptRepository.findByOrder_OrderId(orderId);

        return receipts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    // Giai đoạn 3.2: Tạo Phiếu Xuất (Receipt Creation)
    // Thủ kho kiểm tra rồi ấn tạo phiếu
    public ReceiptResponse createReceipt(Integer orderId, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.PROCESSING && order.getStatus() != OrderStatus.DONE) {
            throw new RuntimeException("Order is not in a valid state to create a receipt");
        }

        Receipt receipt = new Receipt();
        // Generate a random internal code, easily trackable
        receipt.setReceiptCode("REC-" + System.currentTimeMillis());
        receipt.setOrder(order);
        receipt.setStatus(ReceiptStatus.DRAFT);
        receipt.setNote(note);

        // Save the drafting phase
        Receipt savedReceipt = receiptRepository.save(receipt);

        return mapToResponse(savedReceipt);
    }

    @Override
    @Transactional
    // Giai đoạn 3.3: Xác nhận Xuất kho (Dispatched)
    // Thủ kho bấm hoàn tất -> Cập nhật Phiếu -> Trừ thẳng kho
    public ReceiptResponse confirmReceipt(Integer receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Receipt not found with id: " + receiptId));

        if (receipt.getStatus() != ReceiptStatus.DRAFT) {
            throw new RuntimeException("Can only confirm a DRAFT receipt");
        }

        // 1. Chuyển trạng thái Phiếu sang COMPLETED
        receipt.setStatus(ReceiptStatus.COMPLETED);
        receipt.setExportDate(LocalDateTime.now());

        // 2. Lấy ra danh sách các Lô hàng đã bị "giữ chỗ" lúc nãy cho Đơn hàng này
        List<OrderDetailFill> fills = orderDetailFillRepository
                .findByOrderDetail_Order_OrderId(receipt.getOrder().getOrderId());

        // 3. Với từng cục hàng đã nhặt, tiến hành trừ kho thực tế và Lưu Log Lịch sử
        // (Transaction)
        for (OrderDetailFill fill : fills) {
            // Lấy Lô hàng thực trong database
            Inventory inv = inventoryRepository.findByBatchBatchId(fill.getBatch().getBatchId())
                    .orElseThrow(() -> new RuntimeException(
                            "Inventory not found for batch: " + fill.getBatch().getBatchId()));

            // Trừ lượng hàng đã giữ chỗ (do bây giờ Xuất thật sự rồi)
            Float newQuantity = inv.getQuantity() - fill.getQuantity();
            if (newQuantity < 0) {
                throw new RuntimeException("Not enough physical inventory to export for product: "
                        + fill.getOrderDetail().getProduct().getProductName());
            }
            inv.setQuantity(newQuantity);
            inventoryRepository.save(inv);

            // Tạo Transaction Lịch sử Rút hàng (EXPORT)
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(fill.getOrderDetail().getProduct());
            transaction.setBatch(fill.getBatch());
            transaction.setType(InventoryTransactionType.EXPORT);
            transaction.setQuantity(fill.getQuantity()); // Số lượng xuất
            transaction.setReceipt(receipt);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setNote("Exported via Receipt: " + receipt.getReceiptCode());

            inventoryTransactionRepository.save(transaction);
        }

        return mapToResponse(receiptRepository.save(receipt));
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
