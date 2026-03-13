package org.luun.kitchencontrolbev1.service.handler.order;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.service.OrderDetailFillService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessingOrderHandle implements OrderStatusHandler {

    private final OrderDetailFillService orderDetailFillService;

    @Override
    public OrderStatus supportedStatus() {
        return OrderStatus.PROCESSING;
    }

    @Override
    public void handle(Order order) {

        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            throw new IllegalStateException("Order has no details");
        }

        orderDetailFillService.autoAllocateFEFO(order);
    }
}
