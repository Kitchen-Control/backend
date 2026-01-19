package org.luun.hsf.asm01.kitchencontrolbe.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {
    private int storeId;
    private List<OrderDetailRequest> orderDetails; //store place order, typing information of orderDetail

    @Data
    public static class OrderDetailRequest {
        private int productId;
        private float quantity;
    }
}
