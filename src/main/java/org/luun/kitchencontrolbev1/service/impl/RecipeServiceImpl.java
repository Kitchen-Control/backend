package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.RecipeDetailRequest;
import org.luun.kitchencontrolbev1.dto.request.RecipeRequest;
import org.luun.kitchencontrolbev1.dto.response.RecipeDetailResponse;
import org.luun.kitchencontrolbev1.dto.response.RecipeResponse;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.entity.Recipe;
import org.luun.kitchencontrolbev1.entity.RecipeDetail;
import org.luun.kitchencontrolbev1.enums.ProductType;
import org.luun.kitchencontrolbev1.repository.ProductRepository;
import org.luun.kitchencontrolbev1.repository.RecipeDetailRepository;
import org.luun.kitchencontrolbev1.repository.RecipeRepository;
import org.luun.kitchencontrolbev1.service.RecipeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {
    private final RecipeRepository recipeRepository;
    private final ProductRepository productRepository;
    private final RecipeDetailRepository recipeDetailRepository;

    @Override
    public List<RecipeResponse> getRecipes() {
        List<Recipe> recipes = recipeRepository.findAll();
        return recipes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeResponse> searchRecipes(String keyword) {
        List<Recipe> recipes = recipeRepository.findByRecipeNameContainsIgnoreCase(keyword);
        return recipes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional //if having errors during saving, don't save anything to database
    public RecipeResponse createRecipe(RecipeRequest recipeRequest) {
        // 1. Khởi tạo Recipe và set thông tin cơ bản
        Recipe recipe = new Recipe();
        recipe.setRecipeName(recipeRequest.getRecipeName());
        recipe.setYieldQuantity(recipeRequest.getYieldQuantity());
        recipe.setDescription(recipeRequest.getDescription());

        // 2. Tìm và set Sản phẩm chính (Finished Product) cho Recipe
        Product mainProduct = productRepository.findById(recipeRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + recipeRequest.getProductId()));

        //Checking if the product is a FINISHED_PRODUCT
        if (mainProduct.getProductType() != ProductType.FINISHED_PRODUCT) {
            throw new RuntimeException("Product must be a FINISHED_PRODUCT to have a recipe");
        }
        recipe.setProduct(mainProduct);

        // 3. LƯU RECIPE TRƯỚC để có ID
        Recipe savedRecipe = recipeRepository.save(recipe);

        // 4. Xử lý danh sách RecipeDetail
        List<RecipeDetail> recipeDetails = new ArrayList<>();
        if (recipeRequest.getRecipeDetails() != null) {
            for (RecipeDetailRequest detailRequest : recipeRequest.getRecipeDetails()) {
                // Finding raw material by ID
                Product rawMaterial = productRepository.findById(detailRequest.getRawMaterialId())
                        .orElseThrow(() -> new RuntimeException("Raw material not found with id: " + detailRequest.getRawMaterialId()));

                if (rawMaterial.getProductType() != ProductType.RAW_MATERIAL) {
                    throw new RuntimeException("Product " + rawMaterial.getProductName() + " is not a RAW_MATERIAL");
                }

                RecipeDetail detail = new RecipeDetail();
                detail.setRecipe(savedRecipe); // IMPORTANT: Assign the saved Recipe here
                detail.setRawMaterial(rawMaterial);
                detail.setQuantity(detailRequest.getQuantity());

                // Lưu chi tiết vào DB
                recipeDetails.add(recipeDetailRepository.save(detail));
            }
        }

        // 5. Cập nhật lại danh sách chi tiết vào đối tượng đã lưu và trả về
        savedRecipe.setRecipeDetails(recipeDetails);
        return mapToResponse(savedRecipe);
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
