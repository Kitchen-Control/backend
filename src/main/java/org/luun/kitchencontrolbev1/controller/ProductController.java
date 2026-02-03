package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.ProductRequest;
import org.luun.kitchencontrolbev1.dto.response.ProductResponse;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Products API")
@RequestMapping("/products")
@Tag(name = "Products", description = "APIs for managing products (Raw materials and Finished products)")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves a list of all products available in the system.")
    public List<ProductResponse> getProducts() {
        return productService.getProducts();
    }

    @GetMapping("/get-by-type/{productType}")
    @Operation(summary = "Get product by type", description = "Retrieves a product based on its type (e.g., RAW_MATERIAL, FINISHED_PRODUCT).")
    public ProductResponse getProductByType(
            @Parameter(description = "Type of the product", example = "RAW_MATERIAL") @PathVariable String productType) {
        return productService.getProductByType(productType);
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Adds a new product to the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data provided")
    })
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update a product", description = "Updates the details of an existing product identified by its ID.")
    public ProductResponse updateProduct(
            @Parameter(description = "ID of the product to be updated") @PathVariable Integer productId, 
            @RequestBody Product updatedProduct) {
        return productService.updateProduct(productId, updatedProduct);
    }
}
