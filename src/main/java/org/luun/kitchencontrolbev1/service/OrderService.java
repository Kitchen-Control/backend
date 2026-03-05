package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.OrderRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import java.util.List;

public interface OrderService {
    List<OrderResponse> getOrders();

    List<OrderResponse> getOrdersByStoreId(Integer storeId);

    OrderResponse createOrder(OrderRequest request);

    OrderResponse updateOrderStatus(Integer orderId, OrderStatus status);

    List<OrderResponse> getWaitingOrder();

    OrderResponse completeOrder(Integer orderId);

    List<OrderResponse> getOrdersByStatus(OrderStatus orderStatus);

    List<OrderResponse> getOrdersByShipperId(Integer shipperId);
}
