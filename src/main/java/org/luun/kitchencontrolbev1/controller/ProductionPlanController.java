package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanResponse;
import org.luun.kitchencontrolbev1.entity.ProductionPlan;
import org.luun.kitchencontrolbev1.service.ProductionPlanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/production-plans")
@Tag(name = "Production Plans API", description = "APIs for managing production plans")
public class ProductionPlanController {

    private final ProductionPlanService productionPlanService;

    @Operation(summary = "Get all production plans", description = "Retrieves a list of all production plans.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    @GetMapping
    public List<ProductionPlanResponse> getProductionPlans() {
        return productionPlanService.getProductionPlans();
    }

    @Operation(summary = "Get production plan by ID", description = "Retrieves a specific production plan by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved production plan"),
            @ApiResponse(responseCode = "404", description = "Production plan not found")
    })
    @GetMapping("/{id}")
    public ProductionPlanResponse getProductionPlanById(
            @Parameter(description = "ID of the production plan to retrieve") @PathVariable Integer id) {
        return productionPlanService.getProductionPlanById(id);
    }

    @Operation(summary = "Create a new production plan", description = "Creates a new production plan with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created production plan")
    })
    @PostMapping
    public ProductionPlanResponse createProductionPlan(@RequestBody ProductionPlan productionPlan) {
        return productionPlanService.createProductionPlan(productionPlan);
    }
}
