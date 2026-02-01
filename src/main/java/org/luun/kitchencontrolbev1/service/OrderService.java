package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.OrderRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import java.util.List;

public interface OrderService {
    List<OrderResponse> getOrders();
    List<OrderResponse> getOrdersByStoreId(Integer storeId);
    OrderResponse createOrder(OrderRequest orderRequest);
    OrderResponse updateOrderStatus(Integer orderId, OrderStatus status);
}
