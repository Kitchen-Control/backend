package org.luun.kitchencontrolbev1.service.handler.receipt;

import org.luun.kitchencontrolbev1.entity.Receipt;
import org.luun.kitchencontrolbev1.enums.ReceiptStatus;
import org.springframework.stereotype.Component;

@Component
public interface ReceiptStatusHandler {

    ReceiptStatus supportedStatus();

    void handle(Receipt receipt);
}
