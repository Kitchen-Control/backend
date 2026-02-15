package org.luun.kitchencontrolbev1.dto.response;

import lombok.Data;

@Data
public class OrderDetailResponse {
    private Integer orderDetailId;
    private Integer orderId;
    private Integer productId;
    private Float quantity;
}
