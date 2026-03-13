package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.OrderDetailResponse;
import org.luun.kitchencontrolbev1.entity.OrderDetail;

import java.util.List;

public interface OrderDetailService {
    List<OrderDetailResponse> getDetailResponseByOrderId(Integer orderId);

    List<OrderDetail> getDetailsByOrderId(Integer orderId);
}
