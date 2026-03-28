package org.luun.kitchencontrolbev1.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoreResponse {
    Integer storeId;
    String storeName;
    String address;
    String phone;
}
