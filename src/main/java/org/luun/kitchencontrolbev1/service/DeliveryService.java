package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.DeliveryResponse;

import java.util.List;

public interface DeliveryService {
    List<DeliveryResponse> getDeliveries();

    List<DeliveryResponse> getDeliveriesByShipperId(Integer shipperId);

    DeliveryResponse assignShipperToDelivery(Integer deliveryId, Integer shipperId);

    DeliveryResponse createDeliveryWithOrders(List<Integer> orderIds, Integer shipperId);

    DeliveryResponse startDelivery(Integer deliveryId);
}
