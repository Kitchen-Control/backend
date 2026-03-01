package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.luun.kitchencontrolbev1.repository.OrderDetailRepository;
import org.luun.kitchencontrolbev1.service.OrderDetailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;

    @Override
    public List<OrderDetailResponse> getByOrderId(Integer orderId) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder_OrderId(orderId);
        return orderDetails.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderDetailResponse mapToResponse(OrderDetail detail) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderDetailId(detail.getOrderDetailId());

        if (detail.getProduct() != null) {
            response.setProductId(detail.getProduct().getProductId());
            response.setProductName(detail.getProduct().getProductName());
        }

        response.setQuantity(detail.getQuantity());
        return response;
    }
}
