package org.luun.kitchencontrolbev1.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "order_detail_fill")
public class OrderDetailFill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fill_id")
    private Integer fillId;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "order_detail_id", nullable = false)
    private OrderDetail orderDetail;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "batch_id", nullable = false)
    private LogBatch batch;

    @Column(name = "quantity", nullable = false)
    private Float quantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
