package org.luun.kitchencontrolbev1.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.luun.kitchencontrolbev1.enums.ReceiptStatus;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReceiptStatusUpdateRequest {

    private List<Integer> receiptIds;
    private ReceiptStatus status;
}
