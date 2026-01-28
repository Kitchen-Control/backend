package org.luun.kitchencontrolbev1.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "batch_id")
    private LogBatch batch;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private InventoryTransactionType type;

    @Column(name = "quantity")
    private Float quantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}