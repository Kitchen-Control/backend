package org.luun.kitchencontrolbev1.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Integer orderId;
    
    // Delivery info
    Integer deliveryId;
    
    // Store info
    Integer storeId;
    String storeName;
    
    LocalDateTime orderDate;
    OrderStatus status;
    String img;
    String comment;
    
    // Details
    List<OrderDetailResponse> orderDetails;
    
    // Feedback info (optional)
    Integer feedbackId;
    Integer feedbackRating;
    String feedbackComment;
}
