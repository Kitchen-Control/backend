package org.luun.kitchencontrolbev1.service.handler.order;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.repository.OrderRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DoneOrderHandler implements OrderStatusHandler{

    private final OrderRepository orderRepository;

    @Override
    public OrderStatus supportedStatus() {
        return OrderStatus.DONE;
    }

    @Override
    public void handle(Order order) {

        if(order.getParent_order_id() != null){

            Order parent = orderRepository.findById(order.getParent_order_id())
                    .orElseThrow(() -> new IllegalArgumentException("Parent order not found"));
            parent.setStatus(OrderStatus.DONE);
        }
    }
}