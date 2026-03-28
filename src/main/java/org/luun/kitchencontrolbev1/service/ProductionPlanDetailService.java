package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.ProductionPlanDetailRequest;
import org.luun.kitchencontrolbev1.dto.response.ProductionPlanDetailResponse;
import org.luun.kitchencontrolbev1.entity.ProductionPlan;
import org.luun.kitchencontrolbev1.entity.ProductionPlanDetail;

import java.util.List;

public interface ProductionPlanDetailService {
    List<ProductionPlanDetailResponse> getByProductionPlanId(Integer planId);
    ProductionPlanDetail createDetail(ProductionPlanDetailRequest request);
}
