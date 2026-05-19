package com.allinoneshop.service;

import com.allinoneshop.dto.BrandDTO;
import com.allinoneshop.entity.Brand;
import com.allinoneshop.repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock private BrandRepository brandRepository;
    @InjectMocks private BrandService brandService;

    private BrandDTO buildDto(String name) {
        BrandDTO dto = new BrandDTO();
        dto.setName(name);
        return dto;
    }

    @Test
    void createBrand_savesAndReturnsDTO() {
        when(brandRepository.findByName("Nike")).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenAnswer(inv -> {
            Brand b = inv.getArgument(0); b.setId(UUID.randomUUID()); return b;
        });

        BrandDTO result = brandService.createBrand(buildDto("Nike"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Nike");
    }

    @Test
    void createBrand_duplicateName_throwsException() {
        when(brandRepository.findByName("Nike"))
                .thenReturn(Optional.of(Brand.builder().name("Nike").build()));

        assertThatThrownBy(() -> brandService.createBrand(buildDto("Nike")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getAllBrands_returnsAll() {
        when(brandRepository.findAll()).thenReturn(List.of(
                Brand.builder().id(UUID.randomUUID()).name("Nike").build(),
                Brand.builder().id(UUID.randomUUID()).name("Adidas").build()));

        List<BrandDTO> result = brandService.getAllBrands();
        assertThat(result).hasSize(2);
    }

    @Test
    void getBrandById_found() {
        UUID id = UUID.randomUUID();
        when(brandRepository.findById(id))
                .thenReturn(Optional.of(Brand.builder().id(id).name("Puma").build()));

        BrandDTO result = brandService.getBrandById(id);
        assertThat(result.getName()).isEqualTo("Puma");
    }

    @Test
    void getBrandById_notFound_throwsException() {
        when(brandRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> brandService.getBrandById(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Brand not found");
    }

    @Test
    void updateBrand_existingBrand_updatesFields() {
        UUID id = UUID.randomUUID();
        Brand existing = Brand.builder().id(id).name("Old Brand").build();
        when(brandRepository.findById(id)).thenReturn(Optional.of(existing));
        when(brandRepository.save(any(Brand.class))).thenAnswer(inv -> inv.getArgument(0));

        BrandDTO result = brandService.updateBrand(id, buildDto("New Brand"));
        assertThat(result.getName()).isEqualTo("New Brand");
    }

    @Test
    void updateBrand_notFound_throwsException() {
        when(brandRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> brandService.updateBrand(UUID.randomUUID(), buildDto("x")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Brand not found");
    }

    @Test
    void deleteBrand_existingBrand_removesIt() {
        UUID id = UUID.randomUUID();
        when(brandRepository.findById(id))
                .thenReturn(Optional.of(Brand.builder().id(id).name("Delete Me").build()));

        brandService.deleteBrand(id);
        verify(brandRepository).deleteById(id);
    }

    @Test
    void deleteBrand_notFound_throwsException() {
        when(brandRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> brandService.deleteBrand(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Brand not found");
    }
}
