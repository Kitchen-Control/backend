package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.RecipeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeDetailRepository extends JpaRepository<RecipeDetail, Integer> {
    List<RecipeDetail> findByRecipe_Id(Integer recipeId);
}
