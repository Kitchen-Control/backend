package org.luun.kitchencontrolbev1.service.handler.delivery;

import org.luun.kitchencontrolbev1.entity.Delivery;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.enums.DeliveryStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CancelledDeliveryHandler implements DeliveryStatusHandler {

    @Override
    public DeliveryStatus supportedStatus() {
        return DeliveryStatus.CANCEL;
    }

    @Override
    public void handle(Delivery delivery) {
        delivery.setShipper(null);

        List<Order> orders = new ArrayList<>(delivery.getOrders());

        orders.forEach(delivery::removeOrder);
    }
}
