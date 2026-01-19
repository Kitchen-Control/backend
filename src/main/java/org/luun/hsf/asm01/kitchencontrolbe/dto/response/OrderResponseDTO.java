package org.luun.hsf.asm01.kitchencontrolbe.dto.response;

import lombok.Builder;
import lombok.Data;
import org.luun.hsf.asm01.kitchencontrolbe.entity.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDTO {
    private int orderId;
    private int storeId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private List<OrderDetailResponse> orderDetails;

    @Data
    @Builder
    public static class OrderDetailResponse {
        private int productId;
        private float quantity;
    }
}
