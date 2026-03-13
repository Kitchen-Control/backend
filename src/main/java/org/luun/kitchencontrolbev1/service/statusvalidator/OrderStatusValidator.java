package org.luun.kitchencontrolbev1.service.statusvalidator;

import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class OrderStatusValidator {

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.WAITING, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELED),
            OrderStatus.PROCESSING, Set.of(OrderStatus.DISPATCHED),
            OrderStatus.DISPATCHED, Set.of(OrderStatus.DELIVERING),
            OrderStatus.DELIVERING, Set.of(OrderStatus.DONE, OrderStatus.DAMAGED),
            OrderStatus.DONE, Set.of(),
            OrderStatus.CANCELED, Set.of()
    );

    public void validate(OrderStatus current, OrderStatus newStatus) {

        Set<OrderStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());

        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                    "Invalid transition from " + current + " to " + newStatus
            );
        }
    }
}
