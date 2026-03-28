package org.luun.kitchencontrolbev1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialRequirementResponse {
    private Integer productId;
    private String productName;
    private Float totalRequiredQuantity;
    private String unit;
}
