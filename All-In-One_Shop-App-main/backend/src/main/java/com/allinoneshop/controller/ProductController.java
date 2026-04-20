package com.allinoneshop.controller;

import com.allinoneshop.dto.*;
import com.allinoneshop.entity.User;
import com.allinoneshop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product search and retrieval endpoints")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/search")
    @Operation(summary = "Search products with filters")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(
            @RequestBody SearchRequest request,
            @AuthenticationPrincipal User user) {
        UUID userId = user != null ? user.getId() : null;
        List<ProductDTO> products = productService.searchProducts(request, userId);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        SearchRequest request = new SearchRequest();
        List<ProductDTO> products = productService.searchProducts(request, null);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable UUID id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/category/{slug}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable String slug) {
        List<ProductDTO> products = productService.getProductsByCategory(slug);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/brand/{name}")
    @Operation(summary = "Get products by brand")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByBrand(@PathVariable String name) {
        List<ProductDTO> products = productService.getProductsByBrand(name);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}/similar")
    @Operation(summary = "Get similar products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getSimilarProducts(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "4") int limit) {
        List<ProductDTO> products = productService.getSimilarProducts(id, limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getTrendingProducts(
            @RequestParam(defaultValue = "8") int limit) {
        List<ProductDTO> products = productService.getTrendingProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create product")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@RequestBody ProductDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(productService.createProduct(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(@PathVariable UUID id, @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
