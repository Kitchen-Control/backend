package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.WasteLogRequest;
import org.luun.kitchencontrolbev1.dto.response.WasteLogResponse;
import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.entity.WasteLog;
import org.luun.kitchencontrolbev1.repository.LogBatchRepository;
import org.luun.kitchencontrolbev1.repository.OrderRepository;
import org.luun.kitchencontrolbev1.repository.ProductRepository;
import org.luun.kitchencontrolbev1.repository.WasteLogRepository;
import org.luun.kitchencontrolbev1.service.WasteLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WasteLogServiceImpl implements WasteLogService {

    private final WasteLogRepository wasteLogRepository;
    private final ProductRepository productRepository;
    private final LogBatchRepository logBatchRepository;
    private final OrderRepository orderRepository;

    @Override
    public List<WasteLogResponse> getAllWasteLogs() {
        return wasteLogRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WasteLogResponse createWasteLog(WasteLogRequest request) {
        WasteLog log = new WasteLog();
        log.setQuantity(request.getQuantity());
        log.setWasteType(request.getWasteType());
        log.setNote(request.getNote());
        log.setCreatedAt(LocalDateTime.now());
        // Map quan hệ Product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        log.setProduct(product);
        // Map quan hệ Batch (Nếu Có/Cần Thiết)
        if(request.getBatchId() != null){
            LogBatch batch = logBatchRepository.findById(request.getBatchId())
                    .orElseThrow(() -> new RuntimeException("LogBatch not found"));
            log.setBatch(batch);
        }
        // Map quan hệ Order (Nếu Hàng hỏng do lỗi Vận Chuyển/Ship)
        if(request.getOrderId() != null){
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            log.setOrder(order);
        }
        WasteLog savedLog = wasteLogRepository.save(log);
        return mapToResponse(savedLog);
    }

    // Hàm phụ trợ Converter
    private WasteLogResponse mapToResponse(WasteLog log) {
        WasteLogResponse rs = new WasteLogResponse();
        rs.setWasteId(log.getWasteId());

        if(log.getProduct() != null) {
            rs.setProductId(log.getProduct().getProductId());
            rs.setProductName(log.getProduct().getProductName());
        }
        if(log.getBatch() != null) rs.setBatchId(log.getBatch().getBatchId());
        if(log.getOrder() != null) rs.setOrderId(log.getOrder().getOrderId());

        rs.setQuantity(log.getQuantity());
        rs.setWasteType(log.getWasteType());
        rs.setNote(log.getNote());
        rs.setCreatedAt(log.getCreatedAt());
        return rs;
    }
}
