package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.OrderRequest;
import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import java.util.List;

public interface OrderService {
    List<OrderResponse> getOrders();

    Order getOrderById(Integer orderId);

    List<OrderResponse> getOrdersByStoreId(Integer storeId);

    void createOrder(OrderRequest request);

    void updateOrderStatus(Integer orderId, OrderStatus status, String note);

    List<OrderResponse> getOrdersByStatus(OrderStatus orderStatus);

    List<OrderResponse> getOrdersByShipperId(Integer shipperId);
}
