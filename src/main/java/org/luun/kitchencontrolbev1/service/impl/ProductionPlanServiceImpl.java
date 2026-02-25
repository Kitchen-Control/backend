package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanDetailResponse;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanResponse;
import org.luun.kitchencontrolbev1.entity.ProductionPlan;
import org.luun.kitchencontrolbev1.entity.ProductionPlanDetail;
import org.luun.kitchencontrolbev1.repository.ProductionPlanRepository;
import org.luun.kitchencontrolbev1.service.ProductionPlanService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionPlanServiceImpl implements ProductionPlanService {

    private final ProductionPlanRepository productionPlanRepository;

    @Override
    public List<ProductionPlanResponse> getProductionPlans() {
        return productionPlanRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductionPlanResponse getProductionPlanById(Integer id) {
        return mapToResponse(productionPlanRepository.findById(id).orElseThrow(() -> new RuntimeException("Production plan not found")));
    }

    @Override
    public ProductionPlanResponse createProductionPlan(ProductionPlan productionPlan) {
        return mapToResponse(productionPlanRepository.save(productionPlan));
    }


    private ProductionPlanResponse mapToResponse(ProductionPlan productionPlan) {
        ProductionPlanResponse response = new ProductionPlanResponse();
        response.setPlanId(productionPlan.getPlanId());
        response.setPlanDate(productionPlan.getPlanDate());
        response.setStartDate(productionPlan.getStartDate());
        response.setEndDate(productionPlan.getEndDate());
        response.setStatus(productionPlan.getStatus());
        response.setNote(productionPlan.getNote());

        if(productionPlan.getProductionPlanDetails() != null) {
            List<ProductionPlanDetailResponse> details = productionPlan.getProductionPlanDetails()
                    .stream()
                    .map(this::mapToDetailResponse)
                    .collect(Collectors.toList());
            response.setDetails(details);
        }
        return response;
    }

    private ProductionPlanDetailResponse mapToDetailResponse(ProductionPlanDetail detail) {
        ProductionPlanDetailResponse response = new ProductionPlanDetailResponse();
        response.setPlanDetailId(detail.getPlanDetailId());
        response.setProductId(detail.getProduct().getProductId());
        response.setProductName(detail.getProduct().getProductName());
        response.setQuantity(detail.getQuantity());
        response.setNote(detail.getNote());
        return response;
    }
}
