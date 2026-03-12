package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.ReceiptResponse;
import org.luun.kitchencontrolbev1.enums.ReceiptStatus;

import java.util.List;

public interface ReceiptService {
    List<ReceiptResponse> getByOrderId(Integer orderId);
    List<ReceiptResponse> getByStatus(ReceiptStatus status);
    ReceiptResponse createReceipt(Integer orderId, String note);
    void confirmReceipt(List<Integer> receiptId);
    ReceiptResponse updateReceiptStatus(Integer receiptId, ReceiptStatus status);
}
