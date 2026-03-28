package org.luun.kitchencontrolbev1.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data //DTO gốc, là đối tượng cuối cùng được trả về cho frontend.
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FefoSuggestionResponse {
    Integer orderId;
    String storeName;
    List<ProductAllocationSuggestion> productSuggestions;
    boolean isFulfillable;
    String message;

    @Data //Gom nhóm các đề xuất lô hàng cho một sản phẩm cụ thể trong đơn hàng.
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductAllocationSuggestion {
        Integer orderDetailId;
        String productName;
        float requiredQuantity;
        List<BatchSuggestion> batchSuggestions; // Danh sách các lô được đề xuất


        @Data //Chứa thông tin chi tiết về một lô hàng được đề xuất.
        @Builder
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class BatchSuggestion {
            Integer batchId;
            LocalDate expiryDate;
            float availableQuantityInBatch; // Lượng tồn kho hiện tại của lô này
            float suggestedQuantityToPick; // Số lượng hệ thống đề xuất lấy từ lô này
        }
    }
}
