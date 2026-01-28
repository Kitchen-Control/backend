package org.luun.kitchencontrolbev1.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.luun.kitchencontrolbev1.enums.ProductType;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private ProductType productType;

    @Column(name = "unit", length = 255)
    private String unit;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @JsonManagedReference
    @OneToMany(mappedBy = "product")
    private List<Recipe> recipes;

    @JsonManagedReference
    @OneToMany(mappedBy = "rawMaterial")
    private List<RecipeDetail> usedInRecipes;

    @JsonManagedReference
    @OneToMany(mappedBy = "product")
    private List<OrderDetail> orderDetails;

    @JsonManagedReference
    @OneToMany(mappedBy = "product")
    private List<LogBatch> logBatches;

    @JsonManagedReference
    @OneToMany(mappedBy = "product")
    private List<Inventory> inventories;

    @JsonManagedReference
    @OneToMany(mappedBy = "product")
    private List<InventoryTransaction> inventoryTransactions;

    @JsonManagedReference
    @OneToMany(mappedBy = "product")
    private List<ProductionPlanDetail> productionPlanDetails;
}
