package com.allinoneshop.service;

import com.allinoneshop.dto.CategoryDTO;
import com.allinoneshop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class CategoryServiceTest {

    private CategoryRepository categoryRepository;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryRepository = new CategoryRepository();
        categoryService = new CategoryService(categoryRepository);
    }

    private CategoryDTO buildDto(String name) {
        CategoryDTO dto = new CategoryDTO();
        dto.setName(name);
        dto.setSlug(name.toLowerCase().replace(" ", "-"));
        return dto;
    }

    @Test
    void createCategory_savesAndReturnsDTO() {
        CategoryDTO result = categoryService.createCategory(buildDto("Sneakers"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Sneakers");
        assertThat(result.getSlug()).isEqualTo("sneakers");
    }

    @Test
    void createCategory_autoGeneratesSlugIfNull() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Running Shoes");

        CategoryDTO result = categoryService.createCategory(dto);

        assertThat(result.getSlug()).isEqualTo("running-shoes");
    }

    @Test
    void createCategory_duplicateName_throwsException() {
        categoryService.createCategory(buildDto("Shoes"));

        assertThatThrownBy(() -> categoryService.createCategory(buildDto("Shoes")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getAllCategories_returnsAll() {
        categoryService.createCategory(buildDto("Shoes"));
        categoryService.createCategory(buildDto("Tops"));

        List<CategoryDTO> result = categoryService.getAllCategories();
        assertThat(result).hasSize(2);
    }

    @Test
    void getCategoryById_found() {
        CategoryDTO created = categoryService.createCategory(buildDto("Accessories"));

        CategoryDTO result = categoryService.getCategoryById(created.getId());
        assertThat(result.getName()).isEqualTo("Accessories");
    }

    @Test
    void getCategoryById_notFound_throwsException() {
        assertThatThrownBy(() -> categoryService.getCategoryById(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void getCategoryBySlug_found() {
        categoryService.createCategory(buildDto("Outerwear"));

        CategoryDTO result = categoryService.getCategoryBySlug("outerwear");
        assertThat(result.getName()).isEqualTo("Outerwear");
    }

    @Test
    void getCategoryBySlug_notFound_throwsException() {
        assertThatThrownBy(() -> categoryService.getCategoryBySlug("nonexistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void updateCategory_existingCategory_updatesFields() {
        CategoryDTO created = categoryService.createCategory(buildDto("Old"));

        CategoryDTO update = buildDto("New");
        CategoryDTO result = categoryService.updateCategory(created.getId(), update);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getSlug()).isEqualTo("new");
    }

    @Test
    void updateCategory_notFound_throwsException() {
        assertThatThrownBy(() -> categoryService.updateCategory(UUID.randomUUID(), buildDto("x")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void deleteCategory_existingCategory_removesIt() {
        CategoryDTO created = categoryService.createCategory(buildDto("Delete Me"));

        categoryService.deleteCategory(created.getId());
        assertThat(categoryRepository.count()).isZero();
    }

    @Test
    void deleteCategory_notFound_throwsException() {
        assertThatThrownBy(() -> categoryService.deleteCategory(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }
}
