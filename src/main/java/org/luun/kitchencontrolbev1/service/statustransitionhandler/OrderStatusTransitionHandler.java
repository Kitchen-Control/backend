package org.luun.kitchencontrolbev1.service.statustransitionhandler;

import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.service.handler.order.OrderStatusHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderStatusTransitionHandler {

    private final Map<OrderStatus, OrderStatusHandler> handlers;

    public OrderStatusTransitionHandler(List<OrderStatusHandler> handlerList) {

        handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        OrderStatusHandler::supportedStatus,
                        h -> h
                ));
    }

    public void handle(Order order, OrderStatus newStatus) {

        OrderStatusHandler handler = handlers.get(newStatus);

        if (handler != null) {
            handler.handle(order);
        }
    }
}
