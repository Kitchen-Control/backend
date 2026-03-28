package org.luun.kitchencontrolbev1.service.handler.productionplan;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.MaterialRequirementResponse;
import org.luun.kitchencontrolbev1.entity.ProductionPlan;
import org.luun.kitchencontrolbev1.enums.ProductionPlanStatus;
import org.luun.kitchencontrolbev1.repository.InventoryRepository;
import org.luun.kitchencontrolbev1.service.ProductionPlanService;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WaitingPlanStatusHandler implements PlanStatusHandler {

    private final InventoryRepository inventoryRepository;
    
    // Sử dụng @Autowired + @Lazy để tránh lỗi Circular Dependency với PlanStatusTransitionHandler
    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private ProductionPlanService productionPlanService;

    @Override
    public ProductionPlanStatus supportedStatus() {
        return ProductionPlanStatus.WAITING;
    }

    @Override
    public void handle(ProductionPlan plan) {
        // 1. Tính toán tổng nguyên liệu cần thiết cho toàn bộ Kế hoạch
        List<MaterialRequirementResponse> requirements = productionPlanService.getMaterialRequirementsForPlan(plan.getPlanId());
        
        // 2. Kiểm tra tồn kho hiện tại của từng nguyên liệu
        for (MaterialRequirementResponse req : requirements) {
            Float availableInventory = inventoryRepository.getTotalQuantityByProductId(req.getProductId());
            if (availableInventory == null) {
                availableInventory = 0f;
            }
            
            // 3. Nếu số lượng thực tế trong kho < định mức yêu cầu -> Báo lỗi chặn luồng
            if (availableInventory < req.getTotalRequiredQuantity()) {
                throw new IllegalStateException(
                    "Không đủ tồn kho nguyên liệu: [" + req.getProductName() + 
                    "] (Cần: " + String.format("%.2f", req.getTotalRequiredQuantity()) + 
                    ", Hiện có: " + String.format("%.2f", availableInventory) + ")"
                );
            }
        }
    }
}
