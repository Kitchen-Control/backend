package org.luun.kitchencontrolbev1.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryResponse {
    Integer deliveryId;
    LocalDate deliveryDate;
    LocalDateTime createdAt;
    Integer shipperId;
    String shipperName;
    List<OrderResponse> orders;
}
