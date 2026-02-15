package org.luun.kitchencontrolbev1.dto.request;

import lombok.Data;

@Data
public class OrderDetailRequest {
    private Integer orderId;
    private Integer productId;
    private Float quantity;
}
