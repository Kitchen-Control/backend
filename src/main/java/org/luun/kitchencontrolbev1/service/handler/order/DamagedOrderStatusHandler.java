package org.luun.kitchencontrolbev1.service.handler.order;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.luun.kitchencontrolbev1.entity.OrderDetailFill;
import org.luun.kitchencontrolbev1.entity.WasteLog;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.repository.WasteLogRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DamagedOrderStatusHandler implements OrderStatusHandler {
    private final WasteLogRepository wasteLogRepository;

    @Override
    public OrderStatus supportedStatus() {
        return OrderStatus.DAMAGED;
    }
    @Override
    public void handle(Order order) {
        if (order.getOrderDetails() == null) {
            return;
        }
        for (OrderDetail detail : order.getOrderDetails()) {

            // Vì khi chia hàng, 1 món có thể xuất từ nhiều Lô (Batch) khác nhau (lưu ở OrderDetailFill)
            // Ta cần ghi nhận phế phẩm (Waste) chính xác cho từng lô đã xuất đi
            if (detail.getOrderDetailFills() != null) {
                for (OrderDetailFill fill : detail.getOrderDetailFills()) {
                    WasteLog wasteLog = new WasteLog();
                    wasteLog.setOrder(order);                      // Gắn vào đơn hàng bị hỏng
                    wasteLog.setProduct(detail.getProduct());      // Gắn vào Sản phẩm
                    wasteLog.setBatch(fill.getBatch());            // Gắn vào Lô cụ thể bị hỏng
                    wasteLog.setQuantity(fill.getQuantity());      // Ghi nhận số lượng hỏng theo lô
                    wasteLog.setWasteType(OrderStatus.DAMAGED.name());
                    wasteLog.setNote("Tự động báo hỏng (WasteLog) do shipper phản hồi hư hại trong lúc giao.");
                    wasteLog.setCreatedAt(LocalDateTime.now());

                    wasteLogRepository.save(wasteLog);
                }
            } else {
                // Trực phòng ngừa lỗi cho những đơn chưa có Fill batch (Tuy nhiên logic thực tế đã qua xử lý thường sẽ có)
                WasteLog wasteLog = new WasteLog();
                wasteLog.setOrder(order);
                wasteLog.setProduct(detail.getProduct());
                wasteLog.setQuantity(detail.getQuantity());
                wasteLog.setWasteType(OrderStatus.DAMAGED.name());
                wasteLog.setNote("Báo hỏng do shipper (không xác định được lô xuất).");
                wasteLog.setCreatedAt(LocalDateTime.now());
                wasteLogRepository.save(wasteLog);
            }
        }
    }
}
