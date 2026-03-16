package org.luun.kitchencontrolbev1.service.statusvalidator;

import org.luun.kitchencontrolbev1.enums.DeliveryStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class DeliveryStatusValidator {

    private static final Map<DeliveryStatus, Set<DeliveryStatus>> TRANSITIONS = Map.of(
            DeliveryStatus.WAITING, Set.of(DeliveryStatus.DELIVERING, DeliveryStatus.CANCEL),
            DeliveryStatus.DELIVERING, Set.of(DeliveryStatus.DONE),
            DeliveryStatus.DONE, Set.of(),
            DeliveryStatus.CANCEL, Set.of()
    );

    public void validate(DeliveryStatus current, DeliveryStatus newStatus) {

        Set<DeliveryStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());

        if(!allowed.contains(newStatus)) {
            throw new IllegalStateException("Invalid transition from " + current + " to " + newStatus);
        }
    }
}
