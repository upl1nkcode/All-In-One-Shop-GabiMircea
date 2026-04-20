package com.allinoneshop.service;

import com.allinoneshop.dto.BrandDTO;
import com.allinoneshop.repository.BrandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class BrandServiceTest {

    private BrandRepository brandRepository;
    private BrandService brandService;

    @BeforeEach
    void setUp() {
        brandRepository = new BrandRepository();
        brandService = new BrandService(brandRepository);
    }

    private BrandDTO buildDto(String name) {
        BrandDTO dto = new BrandDTO();
        dto.setName(name);
        return dto;
    }

    @Test
    void createBrand_savesAndReturnsDTO() {
        BrandDTO result = brandService.createBrand(buildDto("Nike"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Nike");
    }

    @Test
    void createBrand_duplicateName_throwsException() {
        brandService.createBrand(buildDto("Nike"));

        assertThatThrownBy(() -> brandService.createBrand(buildDto("Nike")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getAllBrands_returnsAll() {
        brandService.createBrand(buildDto("Nike"));
        brandService.createBrand(buildDto("Adidas"));

        List<BrandDTO> result = brandService.getAllBrands();
        assertThat(result).hasSize(2);
    }

    @Test
    void getBrandById_found() {
        BrandDTO created = brandService.createBrand(buildDto("Puma"));

        BrandDTO result = brandService.getBrandById(created.getId());
        assertThat(result.getName()).isEqualTo("Puma");
    }

    @Test
    void getBrandById_notFound_throwsException() {
        assertThatThrownBy(() -> brandService.getBrandById(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Brand not found");
    }

    @Test
    void updateBrand_existingBrand_updatesFields() {
        BrandDTO created = brandService.createBrand(buildDto("Old Brand"));

        BrandDTO result = brandService.updateBrand(created.getId(), buildDto("New Brand"));
        assertThat(result.getName()).isEqualTo("New Brand");
    }

    @Test
    void updateBrand_notFound_throwsException() {
        assertThatThrownBy(() -> brandService.updateBrand(UUID.randomUUID(), buildDto("x")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Brand not found");
    }

    @Test
    void deleteBrand_existingBrand_removesIt() {
        BrandDTO created = brandService.createBrand(buildDto("Delete Me"));

        brandService.deleteBrand(created.getId());
        assertThat(brandRepository.count()).isZero();
    }

    @Test
    void deleteBrand_notFound_throwsException() {
        assertThatThrownBy(() -> brandService.deleteBrand(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Brand not found");
    }
}
