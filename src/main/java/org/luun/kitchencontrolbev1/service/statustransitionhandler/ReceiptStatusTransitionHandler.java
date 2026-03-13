package org.luun.kitchencontrolbev1.service.statustransitionhandler;

import org.luun.kitchencontrolbev1.entity.Receipt;
import org.luun.kitchencontrolbev1.enums.ReceiptStatus;
import org.luun.kitchencontrolbev1.service.handler.receipt.ReceiptStatusHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ReceiptStatusTransitionHandler {

    private final Map<ReceiptStatus, ReceiptStatusHandler> handlers;

    public ReceiptStatusTransitionHandler(List<ReceiptStatusHandler> handlerList) {
        this.handlers = handlerList
                .stream()
                .collect(Collectors.toMap(
                        ReceiptStatusHandler::supportedStatus,
                        h -> h
                ));
    }

    public void handle(Receipt receipt,ReceiptStatus newStatus) {
        ReceiptStatusHandler handler = handlers.get(newStatus);

        if (handler != null) {
            handler.handle(receipt);
        }
    }
}
