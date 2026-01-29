package org.luun.kitchencontrolbev1.dto.response;

import lombok.Data;

@Data
public class ProductResponse {
    private Integer productId;
    private String productName;
    private String productType;
    private String unit;
    private Integer shelfLifeDays;
}
