package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.entity.Receipt;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // This is the explicit and correct way to traverse nested properties.
    // It tells Spring Data JPA to look for the 'store' property in Order,
    // and then the 'storeId' property within that Store entity.
    List<Order> findByStore_StoreId(Integer storeId);

    List<Order> findByStatus(OrderStatus orderStatus);

    List<Order> findByDelivery_Shipper_UserId(Integer shipperId);

    Order findByReceipt_ReceiptId(Integer receiptId);

    @Query("""
            SELECT o FROM Order o 
            WHERE o.orderId IN :ids 
            AND o.delivery IS NULL
            AND o.status = :status            
            """)
    List<Order> findAvailableOrders(
            @Param("ids") List<Integer> ids,
            @Param("status") OrderStatus status
    );

    List<Order> findByDelivery_DeliveryId(Integer deliveryId);

    // Reporting: Count live orders today by status
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.orderDate >= :startOfDay AND o.orderDate <= :endOfDay GROUP BY o.status")
    List<Object[]> countOrdersByStatusToday(@Param("startOfDay") java.time.LocalDateTime startOfDay, @Param("endOfDay") java.time.LocalDateTime endOfDay);

    // Reporting: Order volume by date range
    @Query("SELECT CAST(o.orderDate AS date) as orderDate, COUNT(o) as totalOrders FROM Order o " +
           "WHERE o.orderDate >= :startOfDay AND o.orderDate <= :endOfDay " +
           "GROUP BY CAST(o.orderDate AS date) ORDER BY CAST(o.orderDate AS date) ASC")
    List<Object[]> countOrdersByDateRange(@Param("startOfDay") java.time.LocalDateTime startOfDay, @Param("endOfDay") java.time.LocalDateTime endOfDay);

    // Reporting: Total revenue by store
    @Query("SELECT o.store.storeName, SUM(od.quantity * p.price) " +
           "FROM Order o JOIN o.orderDetails od JOIN od.product p " +
           "WHERE MONTH(o.orderDate) = :month AND YEAR(o.orderDate) = :year AND o.status = 'DONE' " +
           "GROUP BY o.store.storeName ORDER BY SUM(od.quantity * p.price) DESC")
    List<Object[]> calculateRevenueByStore(@Param("month") int month, @Param("year") int year);

    // Reporting: Damaged/Canceled orders
    Page<Order> findByStatusInOrderByOrderDateDesc(List<OrderStatus> statuses, Pageable pageable);
}
