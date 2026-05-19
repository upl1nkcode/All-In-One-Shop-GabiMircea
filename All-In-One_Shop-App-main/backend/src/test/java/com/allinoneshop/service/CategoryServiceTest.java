package com.allinoneshop.service;

import com.allinoneshop.dto.CategoryDTO;
import com.allinoneshop.entity.Category;
import com.allinoneshop.repository.CategoryRepository;
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
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryService categoryService;

    private CategoryDTO buildDto(String name) {
        CategoryDTO dto = new CategoryDTO();
        dto.setName(name);
        dto.setSlug(name.toLowerCase().replace(" ", "-"));
        return dto;
    }

    @Test
    void createCategory_savesAndReturnsDTO() {
        when(categoryRepository.findByName("Sneakers")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0); c.setId(UUID.randomUUID()); return c;
        });

        CategoryDTO result = categoryService.createCategory(buildDto("Sneakers"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Sneakers");
        assertThat(result.getSlug()).isEqualTo("sneakers");
    }

    @Test
    void createCategory_autoGeneratesSlugIfNull() {
        when(categoryRepository.findByName("Running Shoes")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0); c.setId(UUID.randomUUID()); return c;
        });

        CategoryDTO dto = new CategoryDTO();
        dto.setName("Running Shoes");
        CategoryDTO result = categoryService.createCategory(dto);

        assertThat(result.getSlug()).isEqualTo("running-shoes");
    }

    @Test
    void createCategory_duplicateName_throwsException() {
        when(categoryRepository.findByName("Shoes"))
                .thenReturn(Optional.of(Category.builder().name("Shoes").slug("shoes").build()));

        assertThatThrownBy(() -> categoryService.createCategory(buildDto("Shoes")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getAllCategories_returnsAll() {
        when(categoryRepository.findAll()).thenReturn(List.of(
                Category.builder().id(UUID.randomUUID()).name("Shoes").slug("shoes").build(),
                Category.builder().id(UUID.randomUUID()).name("Tops").slug("tops").build()));

        List<CategoryDTO> result = categoryService.getAllCategories();
        assertThat(result).hasSize(2);
    }

    @Test
    void getCategoryById_found() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(Category.builder().id(id).name("Accessories").slug("accessories").build()));

        CategoryDTO result = categoryService.getCategoryById(id);
        assertThat(result.getName()).isEqualTo("Accessories");
    }

    @Test
    void getCategoryById_notFound_throwsException() {
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.getCategoryById(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Category not found");
    }

    @Test
    void getCategoryBySlug_found() {
        when(categoryRepository.findBySlug("outerwear"))
                .thenReturn(Optional.of(Category.builder().id(UUID.randomUUID()).name("Outerwear").slug("outerwear").build()));

        CategoryDTO result = categoryService.getCategoryBySlug("outerwear");
        assertThat(result.getName()).isEqualTo("Outerwear");
    }

    @Test
    void getCategoryBySlug_notFound_throwsException() {
        when(categoryRepository.findBySlug(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.getCategoryBySlug("nonexistent"))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Category not found");
    }

    @Test
    void updateCategory_existingCategory_updatesFields() {
        UUID id = UUID.randomUUID();
        Category existing = Category.builder().id(id).name("Old").slug("old").build();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryDTO result = categoryService.updateCategory(id, buildDto("New"));
        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getSlug()).isEqualTo("new");
    }

    @Test
    void updateCategory_notFound_throwsException() {
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.updateCategory(UUID.randomUUID(), buildDto("x")))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Category not found");
    }

    @Test
    void deleteCategory_existingCategory_removesIt() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(Category.builder().id(id).name("Delete Me").slug("delete-me").build()));

        categoryService.deleteCategory(id);
        verify(categoryRepository).deleteById(id);
    }

    @Test
    void deleteCategory_notFound_throwsException() {
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.deleteCategory(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Category not found");
    }
}
