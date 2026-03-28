package org.luun.kitchencontrolbev1.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    Integer storeId;
    String comment;
    String type;
    List<OrderDetailRequest> orderDetails;
}
