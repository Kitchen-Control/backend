package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    List<Delivery> getDeliveriesByShipperUserId(Integer shipperId);
}
