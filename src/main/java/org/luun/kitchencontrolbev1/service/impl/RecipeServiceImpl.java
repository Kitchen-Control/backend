package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.RecipeDetailResponse;
import org.luun.kitchencontrolbev1.dto.response.RecipeResponse;
import org.luun.kitchencontrolbev1.entity.Recipe;
import org.luun.kitchencontrolbev1.entity.RecipeDetail;
import org.luun.kitchencontrolbev1.repository.RecipeRepository;
import org.luun.kitchencontrolbev1.service.RecipeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {
    private final RecipeRepository recipeRepository;

    @Override
    public List<RecipeResponse> getRecipes() {
        List<Recipe> recipes = recipeRepository.findAll();
        return recipes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RecipeResponse mapToResponse(Recipe recipe) {
        RecipeResponse response = new RecipeResponse();
        response.setId(recipe.getId());
        response.setRecipeName(recipe.getRecipeName());
        response.setYieldQuantity(recipe.getYieldQuantity());
        response.setDescription(recipe.getDescription());
        
        if (recipe.getProduct() != null) {
            response.setProductId(recipe.getProduct().getProductId());
            response.setProductName(recipe.getProduct().getProductName());
        }
        
        if (recipe.getRecipeDetails() != null) {
            List<RecipeDetailResponse> details = recipe.getRecipeDetails().stream()
                    .map(this::mapToDetailResponse)
                    .collect(Collectors.toList());
            response.setRecipeDetails(details);
        }
        
        return response;
    }

    private RecipeDetailResponse mapToDetailResponse(RecipeDetail detail) {
        RecipeDetailResponse response = new RecipeDetailResponse();
        response.setRecipeDetailId(detail.getRecipeDetailId());
        response.setQuantity(detail.getQuantity());
        
        if (detail.getRawMaterial() != null) {
            response.setRawMaterialId(detail.getRawMaterial().getProductId());
            response.setRawMaterialName(detail.getRawMaterial().getProductName());
            response.setUnit(detail.getRawMaterial().getUnit());
        }
        
        return response;
    }
}
