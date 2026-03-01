package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.RecipeDetailResponse;
import org.luun.kitchencontrolbev1.entity.RecipeDetail;
import org.luun.kitchencontrolbev1.repository.RecipeDetailRepository;
import org.luun.kitchencontrolbev1.service.RecipeDetailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeDetailServiceImpl implements RecipeDetailService {

    private final RecipeDetailRepository recipeDetailRepository;

    @Override
    public List<RecipeDetailResponse> getAllRecipeDetails() {
        return recipeDetailRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RecipeDetailResponse getRecipeDetailById(Integer id) {
        RecipeDetail detail = recipeDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RecipeDetail not found with id: " + id));
        return mapToResponse(detail);
    }

    @Override
    public List<RecipeDetailResponse> getRecipeDetailsByRecipeId(Integer recipeId) {
        return recipeDetailRepository.findByRecipe_Id(recipeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RecipeDetailResponse mapToResponse(RecipeDetail detail) {
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
