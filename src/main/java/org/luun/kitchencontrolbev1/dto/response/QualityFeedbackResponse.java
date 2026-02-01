package org.luun.kitchencontrolbev1.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QualityFeedbackResponse {
    Integer feedbackId;
    Integer orderId;
    Integer storeId;
    String storeName;
    Integer rating;
    String comment;
    LocalDateTime createdAt;
}
