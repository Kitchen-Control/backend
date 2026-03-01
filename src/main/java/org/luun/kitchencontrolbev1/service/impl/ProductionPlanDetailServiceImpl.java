package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanDetailResponse;
import org.luun.kitchencontrolbev1.entity.ProductionPlanDetail;
import org.luun.kitchencontrolbev1.repository.ProductionPlanDetailRepository;
import org.luun.kitchencontrolbev1.service.ProductionPlanDetailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionPlanDetailServiceImpl implements ProductionPlanDetailService {

    private final ProductionPlanDetailRepository productionPlanDetailRepository;

    @Override
    public List<ProductionPlanDetailResponse> getByProductionPlanId(Integer planId) {
        List<ProductionPlanDetail> details = productionPlanDetailRepository.findByProductionPlan_PlanId(planId);
        return details.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductionPlanDetailResponse mapToResponse(ProductionPlanDetail detail) {
        ProductionPlanDetailResponse response = new ProductionPlanDetailResponse();
        response.setPlanDetailId(detail.getPlanDetailId());

        if (detail.getProduct() != null) {
            response.setProductId(detail.getProduct().getProductId());
            response.setProductName(detail.getProduct().getProductName());
        }

        response.setQuantity(detail.getQuantity());
        response.setNote(detail.getNote());

        return response;
    }
}
