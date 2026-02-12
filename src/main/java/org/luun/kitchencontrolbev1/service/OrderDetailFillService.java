package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.OrderDetailFillRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailFillResponse;
import org.luun.kitchencontrolbev1.entity.OrderDetailFill;

import java.util.List;

public interface OrderDetailFillService {
    List<OrderDetailFillResponse> getAllOrderDetailFills();
    OrderDetailFillResponse getOrderDetailFillById(Integer id);
    OrderDetailFillResponse createOrderDetailFill(OrderDetailFillRequest request);
    OrderDetailFillResponse updateOrderDetailFill(Integer id, OrderDetailFill updatedOrderDetailFill);
    void deleteOrderDetailFill(Integer id);
}
