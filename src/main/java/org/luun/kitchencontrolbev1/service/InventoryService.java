package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.response.InventoryResponse;
import org.luun.kitchencontrolbev1.entity.Inventory;

import java.util.List;

public interface InventoryService {
    List<InventoryResponse> getInventories();
    InventoryResponse getInventoryById(Integer inventoryId);
}
