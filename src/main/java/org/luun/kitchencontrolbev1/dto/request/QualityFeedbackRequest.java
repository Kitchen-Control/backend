package org.luun.kitchencontrolbev1.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QualityFeedbackRequest {
    Integer orderId;
    Integer rating;
    String comment;
}
