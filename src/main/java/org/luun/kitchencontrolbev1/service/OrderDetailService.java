package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import java.util.List;

public interface OrderDetailService {
    List<OrderDetailResponse> getByOrderId(Integer orderId);
}
