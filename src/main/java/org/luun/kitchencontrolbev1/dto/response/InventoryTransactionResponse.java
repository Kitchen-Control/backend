package org.luun.kitchencontrolbev1.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionResponse {
    Integer transactionId;
    Integer productId;
    String productName;
    Integer batchId;
    InventoryTransactionType type;
    Float quantity;
    LocalDateTime createdAt;
    String note;
}
