package org.luun.kitchencontrolbev1.service.statusvalidator;

import org.luun.kitchencontrolbev1.enums.ReceiptStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ReceiptStatusValidator {

    private static final Map<ReceiptStatus, Set<ReceiptStatus>> TRANSITIONS = Map.of(
            ReceiptStatus.DRAFT, Set.of(ReceiptStatus.CANCELED, ReceiptStatus.COMPLETED),
            ReceiptStatus.CANCELED, Set.of(),
            ReceiptStatus.COMPLETED, Set.of()
    );

    public void validate(ReceiptStatus current, ReceiptStatus newStatus) {
        Set<ReceiptStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());

        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                    "Invalid transition from " + current + " to " + newStatus
            );
        }
    }
}
