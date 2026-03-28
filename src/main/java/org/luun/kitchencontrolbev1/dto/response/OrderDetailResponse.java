package org.luun.kitchencontrolbev1.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.luun.kitchencontrolbev1.entity.OrderDetailFill;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailResponse {
    Integer orderDetailId;
    Integer productId;
    String productName;
    Float quantity;
    Float price; // Thêm trường giá cho từng món
    Float itemTotalPrice; // Tổng giá cho chi tiết này (quantity * price)
    List<OrderDetailFillResponse> orderDetailFills;
}
