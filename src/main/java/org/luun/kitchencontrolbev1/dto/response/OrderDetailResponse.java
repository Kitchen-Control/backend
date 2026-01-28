package org.luun.kitchencontrolbev1.dto.response;

import lombok.Data;

@Data
public class OrderDetailResponse {
    private Integer orderDetailId;
    private Integer productId;
    private String productName;
    private Float quantity;
}
