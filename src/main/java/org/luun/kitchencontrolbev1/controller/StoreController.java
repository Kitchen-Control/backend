package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.StoreRequest;
import org.luun.kitchencontrolbev1.dto.response.StoreResponse;
import org.luun.kitchencontrolbev1.entity.Store;
import org.luun.kitchencontrolbev1.service.StoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Stores API")
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    @Operation(summary = "Get all stores", description = "Retrieves a list of all stores.")
    public List<StoreResponse> getAllStores() {
        return storeService.getAllStores();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get store by ID", description = "Retrieves a specific store by its ID.")
    public StoreResponse getStoreById(@PathVariable Integer id) {
        return storeService.getStoreById(id);
    }

    // Create a new store
    @PostMapping
    @Operation(summary = "Create a new store", description = "Adds a new store to the database.")
    public StoreResponse createStore(@RequestBody StoreRequest request) {
        return storeService.createStore(request);
    }

    // Update an existing store
    @PutMapping("/{id}")
    @Operation(summary = "Update a store", description = "Updates the details of an existing store identified by its ID.")
    public StoreResponse updateStore(@PathVariable Integer id, @RequestBody Store updatedStore) {
        return storeService.updateStore(id, updatedStore);
    }

    // Delete a store
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a store", description = "Deletes an existing store identified by its ID.")
    public void deleteStore(@PathVariable Integer id) {
        storeService.deleteStore(id);
    }

}
