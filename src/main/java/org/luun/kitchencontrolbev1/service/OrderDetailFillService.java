package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.OrderDetailFillResponse;
import java.util.List;

public interface OrderDetailFillService {
    List<OrderDetailFillResponse> getAllOrderDetailFills();

    OrderDetailFillResponse getOrderDetailFillById(Integer fillId);

    List<OrderDetailFillResponse> getOrderDetailFillsByOrderDetailId(Integer orderDetailId);

    List<OrderDetailFillResponse> getOrderDetailFillsByBatchId(Integer batchId);
}
