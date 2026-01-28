package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import java.util.List;

public interface OrderService {
    List<OrderResponse> getOrders();
    List<OrderResponse> getOrdersByStoreId(Integer storeId);
}
