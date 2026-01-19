package org.luun.hsf.asm01.kitchencontrolbe.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.luun.hsf.asm01.kitchencontrolbe.entity.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int orderId;

    // We will map relationships later for simplicity. For now, we use simple IDs.
    @Column(name = "delivery_id")
    private Integer deliveryId;

    @Column(name = "store_id", nullable = false)
    private int storeId;

    @Column(name = "plan_id")
    private Integer planId;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;
}
