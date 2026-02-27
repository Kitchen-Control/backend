package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.ReceiptResponse;
import java.util.List;

public interface ReceiptService {
    ReceiptResponse createReceiptForOrder(Integer orderId, String note);

    ReceiptResponse completeReceipt(Integer receiptId);

    List<ReceiptResponse> getReceiptsByOrderId(Integer orderId);
}
