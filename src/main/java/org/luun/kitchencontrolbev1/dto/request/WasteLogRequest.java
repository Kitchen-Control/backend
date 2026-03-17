package org.luun.kitchencontrolbev1.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WasteLogRequest {
    Integer productId;
    Integer batchId;
    Integer orderId; // Lưu ý, có thể truyền null nếu hủy ở kho chứ ko phải do đơn hàng
    Float quantity;
    String wasteType; // VD: 'EXPIRED', 'DAMAGED_SHIPPING'
    String note;
}
