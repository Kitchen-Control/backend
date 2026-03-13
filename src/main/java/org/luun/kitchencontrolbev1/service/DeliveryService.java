package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.AssignShipperRequest;
import org.luun.kitchencontrolbev1.dto.response.DeliveryResponse;
import org.luun.kitchencontrolbev1.enums.DeliveryStatus;

import java.util.List;

public interface DeliveryService {

    List<DeliveryResponse> getDeliveries();

    List<DeliveryResponse> getDeliveriesByShipperId(Integer shipperId);

    List<DeliveryResponse> getDeliveriesByStatus(DeliveryStatus status);

    DeliveryResponse assignShipperToDelivery(AssignShipperRequest request);

    DeliveryResponse startDelivery(Integer deliveryId);

    DeliveryResponse updateDeliveryStatus(Integer deliveryId, DeliveryStatus status);
}
