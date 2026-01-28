package org.luun.kitchencontrolbev1.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "production_plan_details")
public class ProductionPlanDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_detail_id")
    private Integer planDetailId;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private ProductionPlan productionPlan;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Float quantity;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
