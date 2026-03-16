package org.luun.kitchencontrolbev1.service.handler.order;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.service.OrderService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DoneOrderHandler implements OrderStatusHandler{

    private final OrderService orderService;

    @Override
    public OrderStatus supportedStatus() {
        return OrderStatus.DONE;
    }

    @Override
    public void handle(Order order) {
        if(order.getParent_order_id() != null){
            orderService.updateOrderStatus(
                    order.getParent_order_id(),
                    OrderStatus.DONE, null);

        }
    }
}
