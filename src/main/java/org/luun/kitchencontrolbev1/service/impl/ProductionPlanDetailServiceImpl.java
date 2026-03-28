package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.ProductionPlanDetailRequest;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanDetailResponse;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.entity.ProductionPlan;
import org.luun.kitchencontrolbev1.entity.ProductionPlanDetail;
import org.luun.kitchencontrolbev1.repository.ProductionPlanDetailRepository;
import org.luun.kitchencontrolbev1.service.ProductService;
import org.luun.kitchencontrolbev1.service.ProductionPlanDetailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionPlanDetailServiceImpl implements ProductionPlanDetailService {

    private final ProductionPlanDetailRepository productionPlanDetailRepository;
    private final ProductService productService;

    @Override
    public List<ProductionPlanDetailResponse> getByProductionPlanId(Integer planId) {
        List<ProductionPlanDetail> details = productionPlanDetailRepository.findByProductionPlan_PlanId(planId);
        return details.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductionPlanDetail createDetail(ProductionPlanDetailRequest request) {
        Product product = productService.getProductById(request.getProductId());

        ProductionPlanDetail detail = new ProductionPlanDetail();
        detail.setProduct(product);
        detail.setQuantity(request.getQuantity());
        detail.setNote(request.getNote());
        
        return detail;
    }

    private ProductionPlanDetailResponse mapToResponse(ProductionPlanDetail detail) {
        ProductionPlanDetailResponse response = new ProductionPlanDetailResponse();
        return response.builder()
                .planDetailId(detail.getPlanDetailId())
                .productId(detail.getProduct() != null ? detail.getProduct().getProductId() : null)
                .productName(detail.getProduct() != null ? detail.getProduct().getProductName() : null)
                .quantity(detail.getQuantity())
                .note(detail.getNote())
                .build();
    }
}
