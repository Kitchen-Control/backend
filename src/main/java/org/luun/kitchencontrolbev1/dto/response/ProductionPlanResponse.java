package org.luun.kitchencontrolbev1.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductionPlanResponse {
    Integer planId;
    LocalDate planDate;
    LocalDate startDate;
    LocalDate endDate;
    String status;
    String note;
    List<ProductionPlanDetailResponse> details;
}
