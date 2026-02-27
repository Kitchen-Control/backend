package org.luun.kitchencontrolbev1.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReceiptResponse {
    private Integer receiptId;
    private String receiptCode;
    private Integer orderId;
    private LocalDateTime exportDate;
    private String status;
    private String note;
}
