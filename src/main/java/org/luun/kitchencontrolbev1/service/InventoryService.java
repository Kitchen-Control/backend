package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.InventoryResponse;
import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.enums.ProductType;

import java.util.List;

public interface InventoryService {
    List<InventoryResponse> getInventories();

    InventoryResponse getInventoryById(Integer inventoryId);

    List<InventoryResponse> getInventoryByProductType(ProductType productType);

    Float getAvailableStock(Integer productId);

    Inventory getInventoryByBatchId(Integer batchId);

    Inventory createInventoryFromBatch(LogBatch batch);
}
