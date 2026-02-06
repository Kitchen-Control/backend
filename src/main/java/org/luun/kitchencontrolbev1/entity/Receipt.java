package org.luun.kitchencontrolbev1.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "receipts")
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Integer receiptId;

    @Column(name = "receipt_code", length = 255)
    private String receiptCode;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "export_date")
    private LocalDateTime exportDate;

    @Column(name = "status", length = 255)
    private String status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @JsonManagedReference
    @OneToMany(mappedBy = "receipt")
    private List<InventoryTransaction> inventoryTransactions;
}
