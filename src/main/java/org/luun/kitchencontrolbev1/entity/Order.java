package org.luun.kitchencontrolbev1.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(name = "parent_order_id")
    private Integer parent_order_id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "order_date", columnDefinition = "varchar")
    private LocalDateTime orderDate;

    @Column(name = "type")
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "orders_status")
    private OrderStatus status;

    @Column(name = "img", length = 255)
    private String img;

    @Column(name = "comment", length = 255)
    private String comment;

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails  = new ArrayList<>();

    @JsonManagedReference
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private QualityFeedback qualityFeedback;

    @JsonManagedReference
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Receipt receipt;

    public void addDetail(Product product, float quantity) {

        OrderDetail detail = new OrderDetail();

        detail.setProduct(product);
        detail.setQuantity(quantity);
        detail.setOrder(this);

        this.orderDetails.add(detail);
    }

}
