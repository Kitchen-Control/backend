package org.luun.kitchencontrolbev1.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVolumeResponse {
    private LocalDate date;
    private Long totalOrders;
}
