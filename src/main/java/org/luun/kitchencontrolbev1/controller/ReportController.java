package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.report.IssueOrderResponse;
import org.luun.kitchencontrolbev1.dto.response.report.OrderVolumeResponse;
import org.luun.kitchencontrolbev1.dto.response.report.StoreRevenueResponse;
import org.luun.kitchencontrolbev1.dto.response.report.TopProductResponse;
import org.luun.kitchencontrolbev1.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
@Tag(name = "Report API", description = "API for generating various dashboard and statistical reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/orders/live-status")
    @Operation(summary = "Get live order status counts for today")
    public Map<String, Long> getLiveOrderStatusToday() {
        return reportService.getLiveOrderStatusToday();
    }

    @GetMapping("/orders/volume")
    @Operation(summary = "Get order volume by date range (e.g. startDate=2026-03-01&endDate=2026-03-31)")
    public List<OrderVolumeResponse> getOrderVolume(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return reportService.getOrderVolume(startDate, endDate);
    }

    @GetMapping("/orders/revenue/by-store")
    @Operation(summary = "Get internal revenue by store for a specific month and year")
    public List<StoreRevenueResponse> getRevenueByStore(
            @RequestParam int month,
            @RequestParam int year) {
        return reportService.getRevenueByStore(month, year);
    }

    @GetMapping("/orders/top-products")
    @Operation(summary = "Get top ordered products (sorted by quantity)")
    public List<TopProductResponse> getTopOrderedProducts(
            @RequestParam(defaultValue = "5") int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return reportService.getTopOrderedProducts(pageable);
    }

    @GetMapping("/orders/damaged")
    @Operation(summary = "Get paginated list of canceled or damaged orders")
    public Page<IssueOrderResponse> getDamagedOrCanceledOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reportService.getDamagedOrCanceledOrders(pageable);
    }
}
