package org.luun.kitchencontrolbev1.service.handler.order;

import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.enums.OrderStatus;

public interface OrderStatusHandler {

    OrderStatus supportedStatus();

    void handle(Order order);
}
