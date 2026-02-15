package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.OrderDetailFillRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailFillResponse;
import org.luun.kitchencontrolbev1.entity.OrderDetailFill;
import org.luun.kitchencontrolbev1.repository.LogBatchRepository;
import org.luun.kitchencontrolbev1.repository.OrderDetailFillRepository;
import org.luun.kitchencontrolbev1.repository.OrderDetailRepository;
import org.luun.kitchencontrolbev1.service.OrderDetailFillService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderDetailFillServiceImpl implements OrderDetailFillService {

    private final OrderDetailFillRepository orderDetailFillRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final LogBatchRepository logBatchRepository;

    @Override
    public List<OrderDetailFillResponse> getAllOrderDetailFills() {
        return orderDetailFillRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDetailFillResponse getOrderDetailFillById(Integer id) {
        OrderDetailFill orderDetailFill = orderDetailFillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetailFill not found"));
        return mapToResponse(orderDetailFill);
    }

    @Override
    public OrderDetailFillResponse createOrderDetailFill(OrderDetailFillRequest request) {
        OrderDetailFill orderDetailFill = new OrderDetailFill();
        orderDetailFill.setOrderDetail(orderDetailRepository.findById(request.getOrderDetailId())
                .orElseThrow(() -> new RuntimeException("OrderDetail not found")));
        orderDetailFill.setBatch(logBatchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new RuntimeException("LogBatch not found")));
        orderDetailFill.setQuantity(request.getQuantity());
        
        OrderDetailFill savedOrderDetailFill = orderDetailFillRepository.save(orderDetailFill);
        return mapToResponse(savedOrderDetailFill);
    }

    @Override
    public OrderDetailFillResponse updateOrderDetailFill(Integer id, OrderDetailFill updatedOrderDetailFill) {
        OrderDetailFill existingOrderDetailFill = orderDetailFillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderDetailFill not found"));
        
        existingOrderDetailFill.setQuantity(updatedOrderDetailFill.getQuantity());
        // Update other fields as necessary

        OrderDetailFill savedOrderDetailFill = orderDetailFillRepository.save(existingOrderDetailFill);
        return mapToResponse(savedOrderDetailFill);
    }

    @Override
    public void deleteOrderDetailFill(Integer id) {
        orderDetailFillRepository.deleteById(id);
    }

    private OrderDetailFillResponse mapToResponse(OrderDetailFill orderDetailFill) {
        OrderDetailFillResponse response = new OrderDetailFillResponse();

        response.setFillId(orderDetailFill.getFillId());
        if (orderDetailFill.getOrderDetail() != null) {
            response.setOrderDetailId(orderDetailFill.getOrderDetail().getOrderDetailId());
        }
        if (orderDetailFill.getBatch() != null) {
            response.setBatchId(orderDetailFill.getBatch().getBatchId());
        }
        response.setQuantity(orderDetailFill.getQuantity());
        response.setCreatedAt(orderDetailFill.getCreatedAt());
        return response;
    }
}
