package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.ProductionPlanDetailRequest;
import org.luun.kitchencontrolbev1.dto.request.ProductionPlanRequest;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanDetailResponse;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanResponse;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.entity.ProductionPlan;
import org.luun.kitchencontrolbev1.entity.ProductionPlanDetail;
import org.luun.kitchencontrolbev1.repository.ProductRepository;
import org.luun.kitchencontrolbev1.repository.ProductionPlanRepository;
import org.luun.kitchencontrolbev1.service.ProductionPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionPlanServiceImpl implements ProductionPlanService {

    private final ProductionPlanRepository productionPlanRepository;
    private final ProductRepository productRepository;

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
    @Transactional
    public ProductionPlanResponse createProductionPlan(ProductionPlanRequest request) {
        // 1. Create ProductionPlan from request
        ProductionPlan plan = new ProductionPlan();
        plan.setPlanDate(request.getPlanDate());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setStatus(request.getStatus());
        plan.setNote(request.getNote());

        // 2. Create details and add to the plan using the helper method
        List<ProductionPlanDetail> productionPlanDetails = new ArrayList<>();
        if (request.getDetails() != null) {
            for (ProductionPlanDetailRequest detailRequest : request.getDetails()) {
                Product product = productRepository.findById(detailRequest.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + detailRequest.getProductId()));


                ProductionPlanDetail detail = new ProductionPlanDetail();
                detail.setProduct(product);
                detail.setQuantity(detailRequest.getQuantity());
                detail.setNote(detailRequest.getNote());
                detail.setProductionPlan(plan);

                // *** SỬ DỤNG HELPER METHOD TẠI ĐÂY ***
                /* plan.addProductionPlanDetail(detail); */

                productionPlanDetails.add(detail);
            }
        }
        plan.setProductionPlanDetails(productionPlanDetails);



        // 3. Save the plan (details will be saved by cascade)
        ProductionPlan savedPlan = productionPlanRepository.save(plan);

        return mapToResponse(savedPlan);
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
        if (detail.getProduct() != null) {
            response.setProductId(detail.getProduct().getProductId());
            response.setProductName(detail.getProduct().getProductName());
        }
        response.setQuantity(detail.getQuantity());
        response.setNote(detail.getNote());
        return response;
    }
}
