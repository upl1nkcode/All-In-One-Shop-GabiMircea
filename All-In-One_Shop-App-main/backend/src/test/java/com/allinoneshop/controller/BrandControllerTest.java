package com.allinoneshop.controller;

import com.allinoneshop.dto.BrandDTO;
import com.allinoneshop.service.BrandService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = BrandController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class BrandControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private BrandService brandService;
    @MockBean private com.allinoneshop.security.JwtTokenProvider jwtTokenProvider;
    @MockBean private com.allinoneshop.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private BrandDTO brand(String name) {
        return BrandDTO.builder().id(UUID.randomUUID()).name(name).logoUrl("http://logo.com").build();
    }

    @Test
    void getAllBrands_returns200WithList() throws Exception {
        when(brandService.getAllBrands()).thenReturn(List.of(brand("Nike"), brand("Adidas")));

        mockMvc.perform(get("/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getAllBrands_empty_returns200() throws Exception {
        when(brandService.getAllBrands()).thenReturn(List.of());

        mockMvc.perform(get("/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getBrandById_existing_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(brandService.getBrandById(id)).thenReturn(brand("Puma"));

        mockMvc.perform(get("/brands/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Puma"));
    }

    @Test
    void getBrandById_notFound_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        when(brandService.getBrandById(id)).thenThrow(new RuntimeException("Brand not found"));

        mockMvc.perform(get("/brands/{id}", id))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBrand_validBody_returns200() throws Exception {
        BrandDTO dto = BrandDTO.builder().name("New Brand").build();
        when(brandService.createBrand(any(BrandDTO.class))).thenReturn(brand("New Brand"));

        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createBrand_blankName_returns400() throws Exception {
        BrandDTO dto = BrandDTO.builder().name("").build();

        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBrand_nullName_returns400() throws Exception {
        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBrand_duplicate_returns500() throws Exception {
        BrandDTO dto = BrandDTO.builder().name("Nike").build();
        when(brandService.createBrand(any())).thenThrow(new RuntimeException("Brand with this name already exists"));

        mockMvc.perform(post("/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBrand_validBody_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        BrandDTO dto = BrandDTO.builder().name("Updated Brand").build();
        when(brandService.updateBrand(eq(id), any(BrandDTO.class))).thenReturn(brand("Updated Brand"));

        mockMvc.perform(put("/brands/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Brand"));
    }

    @Test
    void deleteBrand_existing_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(brandService).deleteBrand(id);

        mockMvc.perform(delete("/brands/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(brandService).deleteBrand(id);
    }

    @Test
    void deleteBrand_notFound_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("Brand not found")).when(brandService).deleteBrand(id);

        mockMvc.perform(delete("/brands/{id}", id))
                .andExpect(status().isBadRequest());
    }
}
