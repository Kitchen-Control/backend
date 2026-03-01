package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.ProductionPlanDetailResponse;

import java.util.List;

public interface ProductionPlanDetailService {
    List<ProductionPlanDetailResponse> getByProductionPlanId(Integer planId);
}
