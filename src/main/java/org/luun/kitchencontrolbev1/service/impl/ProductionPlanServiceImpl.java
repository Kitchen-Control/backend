package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.ProductionPlanDetailRequest;
import org.luun.kitchencontrolbev1.dto.request.ProductionPlanRequest;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanDetailResponse;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanResponse;
import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.entity.ProductionPlan;
import org.luun.kitchencontrolbev1.entity.ProductionPlanDetail;
import org.luun.kitchencontrolbev1.enums.DeliveryStatus;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.enums.ProductionPlanStatus;
import org.luun.kitchencontrolbev1.repository.ProductionPlanRepository;
import org.luun.kitchencontrolbev1.service.ProductionPlanDetailService;
import org.luun.kitchencontrolbev1.service.ProductionPlanService;
import org.luun.kitchencontrolbev1.service.statustransitionhandler.PlanStatusTransitionHandler;
import org.luun.kitchencontrolbev1.service.statusvalidator.ProductionPlanStatusValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionPlanServiceImpl implements ProductionPlanService {

    private final ProductionPlanDetailService productionPlanDetailService;

    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionPlanStatusValidator productionPlanStatusValidator;
    private final PlanStatusTransitionHandler planStatusTransitionHandler;


    @Override
    public List<ProductionPlanResponse> getProductionPlans() {
        return productionPlanRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductionPlanResponse getProductionPlanById(Integer id) {
        return mapToResponse(getProductionPlanEntityById(id));
    }

    @Override
    public ProductionPlan getProductionPlanEntityById(Integer id) {
        return productionPlanRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Production plan not found"));
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

        // 2. Create details and add to the plan using the DetailService
        if (request.getDetails() != null) {
            for (ProductionPlanDetailRequest detailRequest : request.getDetails()) {

                ProductionPlanDetail detail = productionPlanDetailService.createDetail(detailRequest);
                plan.addProductionPlanDetail(detail);
            }
        }

        // 3. Save the plan (details will be saved by cascade)
        ProductionPlan savedPlan = productionPlanRepository.save(plan);

        return mapToResponse(savedPlan);
    }

    @Override
    @Transactional
    public ProductionPlanResponse updateProductionPlan(Integer id, ProductionPlanRequest request) {
        // 1. Lấy ProductionPlan từ DB
        ProductionPlan plan = getProductionPlanEntityById(id);

        if (plan.getStatus() != ProductionPlanStatus.DRAFT) {
            throw new IllegalStateException("Production plan is not in DRAFT status");
        }

        // 2. Cập nhật các thông tin cơ bản
        if (request.getStartDate() != null) plan.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) plan.setEndDate(request.getEndDate());
        if (request.getNote() != null) plan.setNote(request.getNote());

        // 3. Cập nhật danh sách details
        if (request.getDetails() != null) {
            // Xóa hết details cũ (vì CascadeType.ALL và orphanRemoval=true ở Entity sẽ lo việc xóa trong DB)
            plan.getProductionPlanDetails().clear();

            // Thêm các details mới từ request bằng cách gọi DetailService
            for (ProductionPlanDetailRequest detailRequest : request.getDetails()) {

                ProductionPlanDetail detail = productionPlanDetailService.createDetail(detailRequest);
                plan.addProductionPlanDetail(detail);
            }
        }

        // 4. Lưu lại plan (JPA sẽ tự động insert các detail mới và delete các detail cũ)
        ProductionPlan updatedPlan = productionPlanRepository.save(plan);

        return mapToResponse(updatedPlan);
    }

    @Override
    @Transactional
    public void updateProductionPlanStatus(Integer id, ProductionPlanStatus newStatus) {
        ProductionPlan plan = getProductionPlanEntityById(id);

        productionPlanStatusValidator.validate(plan.getStatus(), newStatus);

        planStatusTransitionHandler.handle(plan, newStatus);

        plan.setStatus(newStatus);
    }

    @Override
    @Transactional
    public void checkPlanCompletion(ProductionPlan plan) {

        List<LogBatch> batches = plan.getLogBatches();

        if (batches == null || batches.isEmpty()) {
            return;
        }

        boolean allDone = true;
        boolean allDoneOrDamaged = true;

        for (LogBatch batch : batches) {

            if (batch.getStatus() != LogBatchStatus.DONE) {
                allDone = false;
            }

            if (batch.getStatus() != LogBatchStatus.DONE &&
                    batch.getStatus() != LogBatchStatus.DAMAGED) {
                allDoneOrDamaged = false;
            }

            if (!allDone && !allDoneOrDamaged) {
                break;
            }
        }

        if (allDone) {
            plan.setStatus(ProductionPlanStatus.DONE);
        } else if (allDoneOrDamaged) {
            plan.setStatus(ProductionPlanStatus.COMPLETE_ONE_SECTION);
        }
    }

    private ProductionPlanResponse mapToResponse(ProductionPlan productionPlan) {
        ProductionPlanResponse response = new ProductionPlanResponse();
        response.setPlanId(productionPlan.getPlanId());
        response.setPlanDate(productionPlan.getPlanDate());
        response.setStartDate(productionPlan.getStartDate());
        response.setEndDate(productionPlan.getEndDate());
        response.setStatus(productionPlan.getStatus());
        response.setNote(productionPlan.getNote());

        if (productionPlan.getProductionPlanDetails() != null) {
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
