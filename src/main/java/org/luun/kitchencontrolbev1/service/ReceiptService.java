package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.ReceiptResponse;

import java.util.List;

public interface ReceiptService {
    List<ReceiptResponse> getByOrderId(Integer orderId);
}
