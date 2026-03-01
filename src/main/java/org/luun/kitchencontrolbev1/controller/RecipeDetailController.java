package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.RecipeDetailResponse;
import org.luun.kitchencontrolbev1.service.RecipeDetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recipe-details")
@RequiredArgsConstructor
@Tag(name = "Recipe Details API", description = "Operations related to recipe details")
public class RecipeDetailController {

    private final RecipeDetailService recipeDetailService;

    @GetMapping
    @Operation(summary = "Get all recipe details", description = "Retrieve a list of all recipe details")
    public List<RecipeDetailResponse> getAllRecipeDetails() {
        return recipeDetailService.getAllRecipeDetails();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a recipe detail by ID", description = "Retrieve a specific recipe detail by its ID")
    public RecipeDetailResponse getRecipeDetailById(@PathVariable Integer id) {
        return recipeDetailService.getRecipeDetailById(id);
    }

    @GetMapping("/recipe/{recipeId}")
    @Operation(summary = "Get all recipe details by recipe ID", description = "Retrieve a list of recipe details associated with a specific recipe")
    public List<RecipeDetailResponse> getRecipeDetailsByRecipeId(@PathVariable Integer recipeId) {
        return recipeDetailService.getRecipeDetailsByRecipeId(recipeId);
    }
}
