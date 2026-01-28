package org.luun.kitchencontrolbev1.dto.response;

import lombok.Data;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Integer orderId;
    
    // Delivery info
    private Integer deliveryId;
    
    // Store info
    private Integer storeId;
    private String storeName;
    
    private LocalDateTime orderDate;
    private OrderStatus status;
    private String img;
    private String comment;
    
    // Details
    private List<OrderDetailResponse> orderDetails;
    
    // Feedback info (optional)
    private Integer feedbackId;
    private Integer feedbackRating;
    private String feedbackComment;
}
