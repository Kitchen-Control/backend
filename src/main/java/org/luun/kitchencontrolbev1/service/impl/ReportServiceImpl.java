package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.report.IssueOrderResponse;
import org.luun.kitchencontrolbev1.dto.response.report.OrderVolumeResponse;
import org.luun.kitchencontrolbev1.dto.response.report.StoreRevenueResponse;
import org.luun.kitchencontrolbev1.dto.response.report.TopProductResponse;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.repository.OrderDetailRepository;
import org.luun.kitchencontrolbev1.repository.OrderRepository;
import org.luun.kitchencontrolbev1.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public Map<String, Long> getLiveOrderStatusToday() {
        java.time.LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        java.time.LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59, 999999999);
        List<Object[]> results = orderRepository.countOrdersByStatusToday(startOfDay, endOfDay);
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : results) {
            OrderStatus status = (OrderStatus) result[0];
            Long count = (Long) result[1];
            map.put(status.name(), count);
        }
        return map;
    }

    @Override
    public List<OrderVolumeResponse> getOrderVolume(LocalDate startDate, LocalDate endDate) {
        java.time.LocalDateTime startOfDay = startDate.atStartOfDay();
        java.time.LocalDateTime endOfDay = endDate.atTime(23, 59, 59, 999999999);
        List<Object[]> results = orderRepository.countOrdersByDateRange(startOfDay, endOfDay);
        return results.stream().map(result -> {
            LocalDate date;
            if (result[0] instanceof LocalDate) {
                date = (LocalDate) result[0];
            } else if (result[0] instanceof java.sql.Date) {
                date = ((java.sql.Date) result[0]).toLocalDate();
            } else {
                date = LocalDate.parse(result[0].toString());
            }
            Long count = ((Number) result[1]).longValue();
            return OrderVolumeResponse.builder()
                    .date(date)
                    .totalOrders(count)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<StoreRevenueResponse> getRevenueByStore(int month, int year) {
        List<Object[]> results = orderRepository.calculateRevenueByStore(month, year);
        return results.stream().map(result -> {
            String storeName = (String) result[0];
            Double revenue = ((Number) result[1]).doubleValue();
            return StoreRevenueResponse.builder()
                    .storeName(storeName)
                    .totalRevenue(revenue)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<TopProductResponse> getTopOrderedProducts(Pageable pageable) {
        List<Object[]> results = orderDetailRepository.findTopOrderedProducts(pageable);
        return results.stream().map(result -> {
            String productName = (String) result[0];
            Double qty = ((Number) result[1]).doubleValue();
            String unit = (String) result[2];
            return TopProductResponse.builder()
                    .productName(productName)
                    .totalQuantity(qty)
                    .unit(unit)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public Page<IssueOrderResponse> getDamagedOrCanceledOrders(Pageable pageable) {
        List<OrderStatus> statuses = Arrays.asList(OrderStatus.CANCELED, OrderStatus.DAMAGED);
        Page<Order> orders = orderRepository.findByStatusInOrderByOrderDateDesc(statuses, pageable);
        return orders.map(o -> IssueOrderResponse.builder()
                .orderId(o.getOrderId())
                .storeName(o.getStore() != null ? o.getStore().getStoreName() : null)
                .orderDate(o.getOrderDate())
                .status(o.getStatus())
                .comment(o.getComment())
                .build()
        );
    }
}
