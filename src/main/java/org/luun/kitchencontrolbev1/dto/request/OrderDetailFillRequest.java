package org.luun.kitchencontrolbev1.dto.request;

import lombok.Data;

@Data
public class OrderDetailFillRequest {
    private Integer orderDetailId;
    private Integer batchId;
    private Float quantity;
}
