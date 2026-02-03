package org.luun.kitchencontrolbev1.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionRequest {
    Integer productId;
    Integer batchId;
    InventoryTransactionType type;
    Float quantity;
    String note;
}
