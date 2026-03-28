package org.luun.kitchencontrolbev1.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class  ProductionPlanDetailResponse {
    Integer planDetailId;
    Integer productId;
    String productName;
    Float quantity;
    String note;
}
