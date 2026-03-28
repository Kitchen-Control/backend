package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.AssignShipperRequest;
import org.luun.kitchencontrolbev1.dto.response.DeliveryResponse;
import org.luun.kitchencontrolbev1.entity.Delivery;
import org.luun.kitchencontrolbev1.enums.DeliveryStatus;

import java.util.List;

public interface DeliveryService {

    List<DeliveryResponse> getDeliveries();

    List<DeliveryResponse> getDeliveriesByShipperId(Integer shipperId);

    Delivery getDeliveryById(Integer deliveryId);

    void createDelivery(AssignShipperRequest request);

    void updateDeliveryStatus(Integer deliveryId, DeliveryStatus status);
}
