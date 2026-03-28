package org.luun.kitchencontrolbev1.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "recipe_details")
public class RecipeDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_detail_id")
    private Integer recipeDetailId;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "raw_material_id")
    private Product rawMaterial;

    @Column(name = "quantity")
    private Float quantity;
}
