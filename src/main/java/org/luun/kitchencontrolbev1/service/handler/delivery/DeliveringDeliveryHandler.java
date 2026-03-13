package org.luun.kitchencontrolbev1.service.handler.delivery;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.entity.Delivery;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.enums.DeliveryStatus;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.service.OrderService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeliveringDeliveryHandler implements DeliveryStatusHandler{

    private final OrderService orderService;

    @Override
    public DeliveryStatus supportedStatus() {
        return DeliveryStatus.DELIVERING;
    }

    @Override
    public void handle(Delivery delivery) {

        boolean allDispatched = delivery.getOrders()
                .stream()
                .allMatch(order -> order.getStatus() == OrderStatus.DISPATCHED);

        if(!allDispatched) {
            throw new IllegalStateException("Not all orders are dispatched");
        }

        List<Integer> orderIds = delivery.getOrders()
                .stream()
                .map(Order::getOrderId)
                .toList();

        orderService.updateOrderStatus(orderIds, OrderStatus.DELIVERING);
    }
}
