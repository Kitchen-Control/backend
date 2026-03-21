package org.luun.kitchencontrolbev1.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.luun.kitchencontrolbev1.enums.OrderStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueOrderResponse {
    private Integer orderId;
    private String storeName;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private String comment;
}
