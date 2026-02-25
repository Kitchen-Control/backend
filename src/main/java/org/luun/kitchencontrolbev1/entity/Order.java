package org.luun.kitchencontrolbev1.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "orders_status")
    private OrderStatus status;

    @Column(name = "img", length = 255)
    private String img;

    @Column(name = "comment", length = 255)
    private String comment;

    @JsonManagedReference
    @OneToMany(mappedBy = "order")
    private List<OrderDetail> orderDetails;

    @JsonManagedReference
    @OneToOne(mappedBy = "order")
    private QualityFeedback qualityFeedback;

    @JsonManagedReference
    @OneToMany(mappedBy = "order")
    private List<Receipt> receipts;
}
