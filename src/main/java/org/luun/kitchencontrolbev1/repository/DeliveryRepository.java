package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    List<Delivery> getDeliveriesByShipperUserId(Integer shipperId);

    @Query(value = "SELECT * FROM deliveries WHERE status = CAST(:status AS deliveries_status)", nativeQuery = true)
    List<Delivery> getDeliveriesByStatus(@Param("status") String status);
}
