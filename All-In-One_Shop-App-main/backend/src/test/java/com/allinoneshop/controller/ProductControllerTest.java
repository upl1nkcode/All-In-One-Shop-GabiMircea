package com.allinoneshop.controller;

import com.allinoneshop.dto.PagedResponse;
import com.allinoneshop.dto.ProductDTO;
import com.allinoneshop.dto.SearchRequest;
import com.allinoneshop.service.ProductService;
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
        value = ProductController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ProductService productService;
    @MockBean private com.allinoneshop.security.JwtTokenProvider jwtTokenProvider;
    @MockBean private com.allinoneshop.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private ProductDTO product(String name) {
        return ProductDTO.builder().id(UUID.randomUUID()).name(name).isActive(true).build();
    }

    private PagedResponse<ProductDTO> paged(List<ProductDTO> items) {
        return PagedResponse.<ProductDTO>builder()
                .content(items).totalElements(items.size())
                .totalPages(1).page(0).size(20).build();
    }

    // ── GET /products ─────────────────────────────────────────────────────────

    @Test
    void getAllProducts_returns200() throws Exception {
        when(productService.searchProductsPaged(any(SearchRequest.class), isNull()))
                .thenReturn(paged(List.of(product("Hoodie"), product("Sneaker"))));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getAllProducts_withQueryParam_passesQuery() throws Exception {
        when(productService.searchProductsPaged(any(SearchRequest.class), isNull()))
                .thenReturn(paged(List.of(product("Nike Hoodie"))));

        mockMvc.perform(get("/products").param("query", "hoodie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Nike Hoodie"));
    }

    @Test
    void getAllProducts_withPageAndSize_returns200() throws Exception {
        when(productService.searchProductsPaged(any(SearchRequest.class), isNull()))
                .thenReturn(paged(List.of()));

        mockMvc.perform(get("/products").param("page", "1").param("size", "5"))
                .andExpect(status().isOk());
    }

    // ── POST /products/search ─────────────────────────────────────────────────

    @Test
    void searchProducts_emptyBody_returns200() throws Exception {
        when(productService.searchProductsPaged(any(SearchRequest.class), isNull()))
                .thenReturn(paged(List.of(product("Test"))));

        mockMvc.perform(post("/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void searchProducts_withFilters_returns200() throws Exception {
        when(productService.searchProductsPaged(any(SearchRequest.class), isNull()))
                .thenReturn(paged(List.of()));

        SearchRequest req = new SearchRequest();
        req.setQuery("Nike");
        req.setPage(0);
        req.setSize(10);

        mockMvc.perform(post("/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── GET /products/{id} ────────────────────────────────────────────────────

    @Test
    void getProductById_existing_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.getProductById(id)).thenReturn(product("Classic Tee"));

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Classic Tee"));
    }

    @Test
    void getProductById_notFound_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.getProductById(id)).thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isBadRequest());
    }

    // ── GET /products/trending ────────────────────────────────────────────────

    @Test
    void getTrendingProducts_returns200WithList() throws Exception {
        when(productService.getTrendingProducts(8))
                .thenReturn(List.of(product("Trending 1"), product("Trending 2")));

        mockMvc.perform(get("/products/trending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getTrendingProducts_customLimit_callsServiceWithLimit() throws Exception {
        when(productService.getTrendingProducts(3)).thenReturn(List.of());

        mockMvc.perform(get("/products/trending").param("limit", "3"))
                .andExpect(status().isOk());

        verify(productService).getTrendingProducts(3);
    }

    // ── GET /products/{id}/similar ────────────────────────────────────────────

    @Test
    void getSimilarProducts_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.getSimilarProducts(id, 4)).thenReturn(List.of(product("Similar")));

        mockMvc.perform(get("/products/{id}/similar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ── GET /products/category/{slug} ─────────────────────────────────────────

    @Test
    void getProductsByCategory_returns200() throws Exception {
        when(productService.getProductsByCategory("sneakers"))
                .thenReturn(List.of(product("Air Max")));

        mockMvc.perform(get("/products/category/sneakers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Air Max"));
    }

    // ── POST /products ────────────────────────────────────────────────────────

    @Test
    void createProduct_validBody_returns200() throws Exception {
        ProductDTO dto = ProductDTO.builder().name("New Product").build();
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(product("New Product"));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createProduct_blankName_returns400() throws Exception {
        ProductDTO dto = ProductDTO.builder().name("").build();

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_nullName_returns400() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":null}"))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /products/{id} ────────────────────────────────────────────────────

    @Test
    void updateProduct_validBody_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        ProductDTO dto = ProductDTO.builder().name("Updated Name").build();
        when(productService.updateProduct(eq(id), any(ProductDTO.class))).thenReturn(product("Updated Name"));

        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    // ── DELETE /products/{id} ─────────────────────────────────────────────────

    @Test
    void deleteProduct_existing_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(productService).deleteProduct(id);

        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(productService).deleteProduct(id);
    }

    @Test
    void deleteProduct_notFound_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("Product not found")).when(productService).deleteProduct(id);

        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isBadRequest());
    }
}
