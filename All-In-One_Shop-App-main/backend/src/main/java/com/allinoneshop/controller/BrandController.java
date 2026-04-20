package com.allinoneshop.controller;

import com.allinoneshop.dto.*;
import com.allinoneshop.repository.BrandRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@Tag(name = "Brands", description = "Brand information endpoints")
public class BrandController {

    private final BrandRepository brandRepository;

    @GetMapping
    @Operation(summary = "Get all brands")
    public ResponseEntity<ApiResponse<List<BrandDTO>>> getAllBrands() {
        List<BrandDTO> brands = brandRepository.findAll().stream()
                .map(brand -> BrandDTO.builder()
                        .id(brand.getId())
                        .name(brand.getName())
                        .logoUrl(brand.getLogoUrl())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(brands));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID")
    public ResponseEntity<ApiResponse<BrandDTO>> getBrandById(@PathVariable UUID id) {
        return brandRepository.findById(id)
                .map(brand -> ResponseEntity.ok(ApiResponse.success(
                        BrandDTO.builder()
                                .id(brand.getId())
                                .name(brand.getName())
                                .logoUrl(brand.getLogoUrl())
                                .build())))
                .orElse(ResponseEntity.notFound().build());
    }
}
