package org.luun.kitchencontrolbev1.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WasteLogResponse {
    Integer wasteId;
    Integer productId; // Gắn ID các bảng ngoại để mapping sau này
    String productName; // Mở rộng thêm Name báo cho Frontend biết tên SP luôn
    Integer batchId;
    Integer orderId;
    Float quantity;
    String wasteType; //Hiện tạo waste log chưa có enum nên sẽ để cho wasteType nhập tự do
    String note;
    LocalDateTime createdAt;
}