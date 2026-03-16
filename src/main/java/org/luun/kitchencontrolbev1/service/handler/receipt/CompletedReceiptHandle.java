package org.luun.kitchencontrolbev1.service.handler.receipt;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.entity.*;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.enums.ReceiptStatus;
import org.luun.kitchencontrolbev1.service.InventoryService;
import org.luun.kitchencontrolbev1.service.OrderDetailFillService;
import org.luun.kitchencontrolbev1.service.OrderService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CompletedReceiptHandle implements ReceiptStatusHandler {

    private final OrderService orderService;
    private final OrderDetailFillService orderDetailFillService;
    private final InventoryService inventoryService;

    public CompletedReceiptHandle(@Lazy OrderService orderService,
                                  OrderDetailFillService orderDetailFillService,
                                  InventoryService inventoryService) {
        this.orderService = orderService;
        this.orderDetailFillService = orderDetailFillService;
        this.inventoryService = inventoryService;
    }

    @Override
    public ReceiptStatus supportedStatus() {
        return ReceiptStatus.COMPLETED;
    }

    @Override
    public void handle(Receipt receipt) {

        receipt.setExportDate(LocalDateTime.now());

        orderService.updateOrderStatus(receipt.getOrder().getOrderId(), OrderStatus.DISPATCHED, null);

        // Lấy ra danh sách các Lô hàng đã bị "giữ chỗ" lúc nãy cho Đơn hàng này
        List<OrderDetailFill> fills = orderDetailFillService
                .getOrderDetailFillsByOrderId(receipt.getOrder().getOrderId());

        // Với từng cục hàng đã nhặt, tiến hành trừ kho thực tế và Lưu Log Lịch sử
        // (Transaction)
        for (OrderDetailFill fill : fills) {
            // Lấy Lô hàng thực trong database (Inventory)
            Inventory inv = inventoryService.getInventoryByBatchId(fill.getBatch().getBatchId());

            // Trừ lượng hàng đã giữ chỗ
            Float currentQuantity = inv.getQuantity();
            Float quantityToExport = fill.getQuantity();

            inv.setQuantity(currentQuantity - quantityToExport);

            // Tạo Transaction Lịch sử Rút hàng (EXPORT)
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(fill.getOrderDetail().getProduct());
            transaction.setBatch(fill.getBatch());
            transaction.setType(InventoryTransactionType.EXPORT);
            transaction.setQuantity(quantityToExport);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setNote("Exported via Receipt: " + receipt.getReceiptCode());

            receipt.addInventoryTransaction(transaction);
        }
    }
}
