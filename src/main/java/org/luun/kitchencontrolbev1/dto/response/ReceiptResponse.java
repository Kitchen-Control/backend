package org.luun.kitchencontrolbev1.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.luun.kitchencontrolbev1.enums.ReceiptStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReceiptResponse {
    Integer receiptId;
    String receiptCode;
    Integer orderId;
    LocalDateTime exportDate;
    ReceiptStatus status;
    String note;
}
