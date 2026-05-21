package com.allinoneshop.controller;

import com.allinoneshop.dto.CategoryDTO;
import com.allinoneshop.service.CategoryService;
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
        value = CategoryController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CategoryService categoryService;
    @MockBean private com.allinoneshop.security.JwtTokenProvider jwtTokenProvider;
    @MockBean private com.allinoneshop.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private CategoryDTO category(String name, String slug) {
        return CategoryDTO.builder().id(UUID.randomUUID()).name(name).slug(slug).build();
    }

    @Test
    void getAllCategories_returns200() throws Exception {
        when(categoryService.getAllCategories())
                .thenReturn(List.of(category("Sneakers", "sneakers"), category("Hoodies", "hoodies")));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getCategoryById_existing_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.getCategoryById(id)).thenReturn(category("Tops", "tops"));

        mockMvc.perform(get("/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Tops"))
                .andExpect(jsonPath("$.data.slug").value("tops"));
    }

    @Test
    void getCategoryById_notFound_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.getCategoryById(id)).thenThrow(new RuntimeException("Category not found"));

        mockMvc.perform(get("/categories/{id}", id))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCategoryBySlug_existing_returns200() throws Exception {
        when(categoryService.getCategoryBySlug("sneakers")).thenReturn(category("Sneakers", "sneakers"));

        mockMvc.perform(get("/categories/slug/sneakers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Sneakers"));
    }

    @Test
    void getCategoryBySlug_notFound_returns500() throws Exception {
        when(categoryService.getCategoryBySlug("unknown"))
                .thenThrow(new RuntimeException("Category not found"));

        mockMvc.perform(get("/categories/slug/unknown"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_validBody_returns200() throws Exception {
        CategoryDTO dto = CategoryDTO.builder().name("Bottoms").slug("bottoms").build();
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(category("Bottoms", "bottoms"));

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value("bottoms"));
    }

    @Test
    void createCategory_blankName_returns400() throws Exception {
        CategoryDTO dto = CategoryDTO.builder().name("").build();

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_duplicate_returns500() throws Exception {
        CategoryDTO dto = CategoryDTO.builder().name("Sneakers").build();
        when(categoryService.createCategory(any()))
                .thenThrow(new RuntimeException("Category with this name already exists"));

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_validBody_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryDTO dto = CategoryDTO.builder().name("Updated Category").build();
        when(categoryService.updateCategory(eq(id), any())).thenReturn(category("Updated Category", "updated-category"));

        mockMvc.perform(put("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCategory_existing_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(categoryService).deleteCategory(id);

        mockMvc.perform(delete("/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteCategory_notFound_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("Category not found")).when(categoryService).deleteCategory(id);

        mockMvc.perform(delete("/categories/{id}", id))
                .andExpect(status().isBadRequest());
    }
}
