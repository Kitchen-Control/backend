package org.luun.kitchencontrolbev1.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRevenueResponse {
    private String storeName;
    private Double totalRevenue;
}
