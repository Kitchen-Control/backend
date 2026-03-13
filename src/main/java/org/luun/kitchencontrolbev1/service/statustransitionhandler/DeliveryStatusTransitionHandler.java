package org.luun.kitchencontrolbev1.service.statustransitionhandler;

import org.luun.kitchencontrolbev1.entity.Delivery;
import org.luun.kitchencontrolbev1.enums.DeliveryStatus;
import org.luun.kitchencontrolbev1.service.handler.delivery.DeliveryStatusHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DeliveryStatusTransitionHandler {

    private final Map<DeliveryStatus, DeliveryStatusHandler> handlers;

    public DeliveryStatusTransitionHandler(List<DeliveryStatusHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        DeliveryStatusHandler::supportedStatus,
                        h -> h
                ));
    }

    public void handle(Delivery delivery, DeliveryStatus newStatus) {

        DeliveryStatusHandler handler = handlers.get(newStatus);

        if (handler != null) {
            handler.handle(delivery);
        }

    }
}
