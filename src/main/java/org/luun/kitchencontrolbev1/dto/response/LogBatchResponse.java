package org.luun.kitchencontrolbev1.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.enums.LogBatchType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LogBatchResponse {
    Integer batchId;
    Integer planId;
    Integer productId;
    String productName;
    Float quantity;
    LocalDate productionDate;
    LocalDate expiryDate;
    LogBatchStatus status;
    LogBatchType type;
    LocalDateTime createdAt;
}
