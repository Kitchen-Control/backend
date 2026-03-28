package org.luun.kitchencontrolbev1.service.handler.delivery;

import org.luun.kitchencontrolbev1.entity.Delivery;
import org.luun.kitchencontrolbev1.enums.DeliveryStatus;
import org.springframework.stereotype.Component;

@Component
public interface DeliveryStatusHandler {

    DeliveryStatus supportedStatus();

    void handle(Delivery delivery);
}
