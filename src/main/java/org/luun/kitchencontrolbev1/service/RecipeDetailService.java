package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.RecipeDetailResponse;

import java.util.List;

public interface RecipeDetailService {
    List<RecipeDetailResponse> getAllRecipeDetails();

    RecipeDetailResponse getRecipeDetailById(Integer id);

    List<RecipeDetailResponse> getRecipeDetailsByRecipeId(Integer recipeId);
}
