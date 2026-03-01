package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailFillResponse;
import org.luun.kitchencontrolbev1.entity.OrderDetailFill;
import org.luun.kitchencontrolbev1.repository.OrderDetailFillRepository;
import org.luun.kitchencontrolbev1.service.OrderDetailFillService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderDetailFillServiceImpl implements OrderDetailFillService {

    private final OrderDetailFillRepository orderDetailFillRepository;

    @Override
    public List<OrderDetailFillResponse> getAllOrderDetailFills() {
        return orderDetailFillRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDetailFillResponse getOrderDetailFillById(Integer fillId) {
        OrderDetailFill fill = orderDetailFillRepository.findById(fillId)
                .orElseThrow(() -> new RuntimeException("OrderDetailFill not found with id: " + fillId));
        return mapToResponse(fill);
    }

    @Override
    public List<OrderDetailFillResponse> getOrderDetailFillsByOrderDetailId(Integer orderDetailId) {
        return orderDetailFillRepository.findByOrderDetail_OrderDetailId(orderDetailId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDetailFillResponse> getOrderDetailFillsByBatchId(Integer batchId) {
        return orderDetailFillRepository.findByBatch_BatchId(batchId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderDetailFillResponse mapToResponse(OrderDetailFill fill) {
        OrderDetailFillResponse response = new OrderDetailFillResponse();
        response.setFillId(fill.getFillId());

        if (fill.getOrderDetail() != null) {
            response.setOrderDetailId(fill.getOrderDetail().getOrderDetailId());
        }

        if (fill.getBatch() != null) {
            response.setBatchId(fill.getBatch().getBatchId());
        }

        response.setQuantity(fill.getQuantity());
        response.setCreatedAt(fill.getCreatedAt());

        return response;
    }
}
