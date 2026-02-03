package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.RecipeRequest;
import org.luun.kitchencontrolbev1.dto.response.RecipeResponse;
import java.util.List;

public interface RecipeService {
    List<RecipeResponse> getRecipes();
    List<RecipeResponse> searchRecipes(String keyword);
    RecipeResponse createRecipe(RecipeRequest recipeRequest);
}
