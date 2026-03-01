package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanDetailResponse;
import org.luun.kitchencontrolbev1.service.ProductionPlanDetailService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/production-plan-details")
@Tag(name = "Production Plan Details API", description = "API for managing production plan details")
public class ProductionPlanDetailController {

    private final ProductionPlanDetailService productionPlanDetailService;

    @GetMapping("/plan/{planId}")
    @Operation(summary = "Get all production plan details by Plan ID")
    public List<ProductionPlanDetailResponse> getByProductionPlanId(@PathVariable Integer planId) {
        return productionPlanDetailService.getByProductionPlanId(planId);
    }
}
