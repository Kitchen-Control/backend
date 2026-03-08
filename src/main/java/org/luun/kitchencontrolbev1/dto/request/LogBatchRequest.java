package org.luun.kitchencontrolbev1.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.enums.LogBatchType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LogBatchRequest {
    Integer planId;
    Integer productId;
    Float quantity;
    LocalDate productionDate;
    LocalDate expiryDate;
    LogBatchType type;
}
