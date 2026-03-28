package org.luun.kitchencontrolbev1.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShipperResponse {
    Integer userId;
    String username;
    String fullName;
    String roleName;
    List<DeliveryResponse> deliveries;
}
