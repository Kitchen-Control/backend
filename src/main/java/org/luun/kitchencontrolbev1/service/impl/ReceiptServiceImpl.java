package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.ReceiptResponse;
import org.luun.kitchencontrolbev1.entity.Receipt;
import org.luun.kitchencontrolbev1.repository.ReceiptRepository;
import org.luun.kitchencontrolbev1.service.ReceiptService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepository;

    @Override
    public List<ReceiptResponse> getByOrderId(Integer orderId) {
        List<Receipt> receipts = receiptRepository.findByOrder_OrderId(orderId);

        return receipts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReceiptResponse mapToResponse(Receipt receipt) {
        ReceiptResponse response = new ReceiptResponse();
        response.setReceiptId(receipt.getReceiptId());
        response.setReceiptCode(receipt.getReceiptCode());

        if (receipt.getOrder() != null) {
            response.setOrderId(receipt.getOrder().getOrderId());
        }

        response.setExportDate(receipt.getExportDate());
        response.setStatus(receipt.getStatus());
        response.setNote(receipt.getNote());

        return response;
    }
}
