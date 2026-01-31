package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.InventoryResponse;
import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.service.impl.InventoryServiceImpl;
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

    private final InventoryServiceImpl inventoryService;

    @GetMapping
    public List<InventoryResponse> getInventories() {
        return inventoryService.getInventories();
    }

    @GetMapping("/get-by-id/{inventoryId}")
    public InventoryResponse getInventoryById(@PathVariable Integer inventoryId) {
        return inventoryService.getInventoryById(inventoryId);
    }
}
