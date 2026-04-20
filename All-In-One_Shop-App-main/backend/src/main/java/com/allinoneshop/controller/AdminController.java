package com.allinoneshop.controller;

import com.allinoneshop.dto.ApiResponse;
import com.allinoneshop.dto.ProductDTO;
import com.allinoneshop.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administration and ingestion endpoints")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
    }

    @PostMapping("/ingest")
    @Operation(summary = "Ingest scraped product data")
    public ResponseEntity<ApiResponse<ProductDTO>> ingestProduct(@RequestBody Map<String, Object> payload) {
        ProductDTO ingested = adminService.ingestProduct(payload);
        return ResponseEntity.ok(ApiResponse.success(ingested));
    }

    @PostMapping("/scrape")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger scraper to populate products from all stores")
    public ResponseEntity<ApiResponse<Map<String, Object>>> runScraper() {
        Map<String, Object> result = adminService.runScraper();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
