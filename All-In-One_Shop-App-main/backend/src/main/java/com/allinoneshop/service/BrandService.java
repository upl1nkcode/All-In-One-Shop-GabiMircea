package com.allinoneshop.service;

import com.allinoneshop.dto.BrandDTO;
import com.allinoneshop.entity.Brand;
import com.allinoneshop.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public List<BrandDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BrandDTO getBrandById(UUID id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        return convertToDTO(brand);
    }

    public BrandDTO createBrand(BrandDTO dto) {
        if (brandRepository.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Brand with this name already exists");
        }
        Brand brand = Brand.builder()
                .name(dto.getName())
                .logoUrl(dto.getLogoUrl())
                .build();
        brand = brandRepository.save(brand);
        return convertToDTO(brand);
    }

    public BrandDTO updateBrand(UUID id, BrandDTO dto) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        brand.setName(dto.getName());
        brand.setLogoUrl(dto.getLogoUrl());
        brand = brandRepository.save(brand);
        return convertToDTO(brand);
    }

    public void deleteBrand(UUID id) {
        brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        brandRepository.deleteById(id);
    }

    private BrandDTO convertToDTO(Brand brand) {
        return BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .logoUrl(brand.getLogoUrl())
                .build();
    }
}
