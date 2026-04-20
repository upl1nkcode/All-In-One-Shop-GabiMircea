package com.allinoneshop.controller;

import com.allinoneshop.dto.*;
import com.allinoneshop.repository.ProductPriceRepository;
import com.allinoneshop.repository.StoreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.allinoneshop.entity.Store;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Tag(name = "Stores", description = "Store information endpoints")
public class StoreController {

    private final StoreRepository storeRepository;
    private final ProductPriceRepository priceRepository;

    @GetMapping
    @Operation(summary = "Get all stores")
    public ResponseEntity<ApiResponse<List<StoreDTO>>> getAllStores() {
        List<StoreDTO> stores = storeRepository.findAll().stream()
                .map(store -> StoreDTO.builder()
                        .id(store.getId())
                        .name(store.getName())
                        .website(store.getWebsite())
                        .logoUrl(store.getLogoUrl())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(stores));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get store by ID")
    public ResponseEntity<ApiResponse<StoreDTO>> getStoreById(@PathVariable UUID id) {
        return storeRepository.findById(id)
                .map(store -> ResponseEntity.ok(ApiResponse.success(
                        StoreDTO.builder()
                                .id(store.getId())
                                .name(store.getName())
                                .website(store.getWebsite())
                                .logoUrl(store.getLogoUrl())
                                .build())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create store")
    public ResponseEntity<ApiResponse<StoreDTO>> createStore(@RequestBody StoreDTO dto) {
        Store store = new Store();
        store.setName(dto.getName());
        store.setWebsite(dto.getWebsite());
        store.setLogoUrl(dto.getLogoUrl());
        store = storeRepository.save(store);
        dto.setId(store.getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update store")
    public ResponseEntity<ApiResponse<StoreDTO>> updateStore(@PathVariable UUID id, @RequestBody StoreDTO dto) {
        return storeRepository.findById(id).map(store -> {
            store.setName(dto.getName());
            store.setWebsite(dto.getWebsite());
            store.setLogoUrl(dto.getLogoUrl());
            store = storeRepository.save(store);
            dto.setId(store.getId());
            return ResponseEntity.ok(ApiResponse.success(dto));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "Delete store")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable UUID id) {
        priceRepository.deleteByStoreId(id);
        storeRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
