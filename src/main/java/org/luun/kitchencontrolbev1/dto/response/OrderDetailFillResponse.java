package org.luun.kitchencontrolbev1.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderDetailFillResponse {
    private Integer fillId;
    private Integer orderDetailId;
    private Integer batchId;
    private Float quantity;
    private LocalDateTime createdAt;
}
