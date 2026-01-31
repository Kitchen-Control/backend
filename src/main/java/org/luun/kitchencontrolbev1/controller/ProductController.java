package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.ProductResponse;
import org.luun.kitchencontrolbev1.entity.Product;
import org.luun.kitchencontrolbev1.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Products API")
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    //Getting all products
    @GetMapping
    public List<ProductResponse> getProducts() {
        return productService.getProducts();
    }

    //Getting a product by type
    @GetMapping("/get-by-type/{productType}")
    public ProductResponse getProductByType(@PathVariable String productType) {
        return productService.getProductByType(productType);
    }

    //Searching products by name
    @GetMapping("/search-by-name/{keyword}")
    public List<ProductResponse> searchProductByName(@PathVariable String keyword) {
        return productService.searchProductByProductName(keyword);
    }

    //Creating a new product
    @PostMapping
    public ProductResponse createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    //Updating a product
    @PutMapping("/{productId}")
    public ProductResponse updateProduct(@PathVariable Integer productId, @RequestBody Product updatedProduct) {
        return productService.updateProduct(productId, updatedProduct);
    }
}
