package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.OrderDetailRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import org.luun.kitchencontrolbev1.entity.OrderDetail;

import java.util.List;

public interface OrderDetailService {
    List<OrderDetailResponse> getAllOrderDetails();
    OrderDetailResponse getOrderDetailById(Integer id);
    OrderDetailResponse createOrderDetail(OrderDetailRequest request);
    OrderDetailResponse updateOrderDetail(Integer id, OrderDetail updatedOrderDetail);
    void deleteOrderDetail(Integer id);
}
