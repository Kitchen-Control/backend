package org.luun.kitchencontrolbev1.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "production_plans")
public class ProductionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Integer planId;

    @Column(name = "plan_date")
    private LocalDate planDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", length = 255)
    private String status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @JsonManagedReference
    @OneToMany(mappedBy = "productionPlan")
    private List<ProductionPlanDetail> productionPlanDetails;

    @JsonManagedReference
    @OneToMany(mappedBy = "plan")
    private List<LogBatch> logBatches;
}
