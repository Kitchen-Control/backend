package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.InventoryResponse;
import org.luun.kitchencontrolbev1.enums.ProductType;
import org.luun.kitchencontrolbev1.service.InventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Inventories API")
@RequestMapping("/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "Get all inventories")
    public List<InventoryResponse> getInventories() {
        return inventoryService.getInventories();
    }

    @GetMapping("/get-by-id/{inventoryId}")
    @Operation(summary = "Get inventory by ID")
    public InventoryResponse getInventoryById(@PathVariable Integer inventoryId) {
        return inventoryService.getInventoryById(inventoryId);
    }

    @GetMapping("/type/{productType}")
    @Operation(summary = "Get inventories by Product Type")
    public List<InventoryResponse> getInventoryByProductType(@PathVariable ProductType productType) {
        return inventoryService.getInventoryByProductType(productType);
    }
}
