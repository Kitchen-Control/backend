package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.report.IssueOrderResponse;
import org.luun.kitchencontrolbev1.dto.response.report.OrderVolumeResponse;
import org.luun.kitchencontrolbev1.dto.response.report.StoreRevenueResponse;
import org.luun.kitchencontrolbev1.dto.response.report.TopProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {
    Map<String, Long> getLiveOrderStatusToday();
    List<OrderVolumeResponse> getOrderVolume(LocalDate startDate, LocalDate endDate);
    List<StoreRevenueResponse> getRevenueByStore(int month, int year);
    List<TopProductResponse> getTopOrderedProducts(Pageable pageable);
    Page<IssueOrderResponse> getDamagedOrCanceledOrders(Pageable pageable);
}
