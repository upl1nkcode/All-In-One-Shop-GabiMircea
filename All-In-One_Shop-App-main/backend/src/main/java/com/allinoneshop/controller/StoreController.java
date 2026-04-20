package com.allinoneshop.controller;

import com.allinoneshop.dto.*;
import com.allinoneshop.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Tag(name = "Stores", description = "Store information endpoints")
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    @Operation(summary = "Get all stores")
    public ResponseEntity<ApiResponse<List<StoreDTO>>> getAllStores() {
        List<StoreDTO> stores = storeService.getAllStores();
        return ResponseEntity.ok(ApiResponse.success(stores));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get store by ID")
    public ResponseEntity<ApiResponse<StoreDTO>> getStoreById(@PathVariable UUID id) {
        StoreDTO store = storeService.getStoreById(id);
        return ResponseEntity.ok(ApiResponse.success(store));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create store")
    public ResponseEntity<ApiResponse<StoreDTO>> createStore(@Valid @RequestBody StoreDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(storeService.createStore(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update store")
    public ResponseEntity<ApiResponse<StoreDTO>> updateStore(
            @PathVariable UUID id, @Valid @RequestBody StoreDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(storeService.updateStore(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete store")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable UUID id) {
        storeService.deleteStore(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
