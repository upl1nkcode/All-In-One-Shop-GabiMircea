package com.allinoneshop.service;

import com.allinoneshop.dto.BrandDTO;
import com.allinoneshop.dto.ProductDTO;
import com.allinoneshop.dto.SearchRequest;
import com.allinoneshop.entity.Brand;
import com.allinoneshop.entity.Category;
import com.allinoneshop.entity.Product;
import com.allinoneshop.entity.ProductPrice;
import com.allinoneshop.entity.enums.Gender;
import com.allinoneshop.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SearchHistoryRepository searchHistoryRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    // ── helpers ──────────────────────────────────────────────

    private Product buildProduct(UUID id, String name) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setIsActive(true);
        p.setGender(Gender.UNISEX);
        p.setPrices(new ArrayList<>());
        return p;
    }

    private Product buildProductWithPrice(UUID id, String name, BigDecimal price) {
        Product p = buildProduct(id, name);
        ProductPrice pp = new ProductPrice();
        pp.setPrice(price);
        com.allinoneshop.entity.Store store = new com.allinoneshop.entity.Store();
        store.setId(UUID.randomUUID());
        store.setName("Test Store");
        store.setWebsite("https://test.com");
        store.setIsActive(true);
        pp.setStore(store);
        p.setPrices(List.of(pp));
        return p;
    }

    private ProductDTO buildDto(String name) {
        ProductDTO dto = new ProductDTO();
        dto.setName(name);
        dto.setGender("UNISEX");
        return dto;
    }

    // ── createProduct ─────────────────────────────────────────

    @Test
    void createProduct_savesAndReturnsDTO() {
        ProductDTO dto = buildDto("Classic Hoodie");
        UUID newId = UUID.randomUUID();
        Product saved = buildProduct(newId, "Classic Hoodie");

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDTO result = productService.createProduct(dto);

        assertThat(result.getName()).isEqualTo("Classic Hoodie");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_withValidBrandId_looksBrandUp() {
        UUID brandId = UUID.randomUUID();
        Brand brand = new Brand();
        brand.setId(brandId);
        brand.setName("Nike");

        ProductDTO dto = new ProductDTO();
        dto.setName("Nike Shoe");
        dto.setBrandId(brandId);
        dto.setGender("UNISEX");

        Product saved = buildProduct(UUID.randomUUID(), "Nike Shoe");
        saved.setBrand(brand);

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
        when(productRepository.save(any())).thenReturn(saved);

        ProductDTO result = productService.createProduct(dto);

        verify(brandRepository).findById(brandId);
        assertThat(result.getBrand()).isNotNull();
        assertThat(result.getBrand().getName()).isEqualTo("Nike");
    }

    @Test
    void createProduct_withValidCategoryId_looksCategoryUp() {
        UUID catId = UUID.randomUUID();
        Category cat = new Category();
        cat.setId(catId);
        cat.setName("Sneakers");
        cat.setSlug("sneakers");

        ProductDTO dto = new ProductDTO();
        dto.setName("Air Max");
        dto.setCategoryId(catId);
        dto.setGender("UNISEX");

        Product saved = buildProduct(UUID.randomUUID(), "Air Max");
        saved.setCategory(cat);

        when(categoryRepository.findById(catId)).thenReturn(Optional.of(cat));
        when(productRepository.save(any())).thenReturn(saved);

        ProductDTO result = productService.createProduct(dto);

        verify(categoryRepository).findById(catId);
        assertThat(result.getCategory()).isNotNull();
        assertThat(result.getCategory().getName()).isEqualTo("Sneakers");
    }

    @Test
    void createProduct_setsIsActiveTrue() {
        ProductDTO dto = buildDto("New Product");
        Product saved = buildProduct(UUID.randomUUID(), "New Product");

        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            assertThat(p.getIsActive()).isTrue();
            return saved;
        });

        productService.createProduct(dto);
    }

    // ── updateProduct ─────────────────────────────────────────

    @Test
    void updateProduct_existingProduct_updatesAndReturnsDTO() {
        UUID id = UUID.randomUUID();
        Product existing = buildProduct(id, "Old Name");
        Product saved = buildProduct(id, "New Name");
        ProductDTO dto = buildDto("New Name");

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(any())).thenReturn(saved);

        ProductDTO result = productService.updateProduct(id, dto);

        assertThat(result.getName()).isEqualTo("New Name");
        verify(productRepository).save(any());
    }

    @Test
    void updateProduct_notFound_throwsRuntimeException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(id, buildDto("x")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    // ── deleteProduct ─────────────────────────────────────────

    @Test
    void deleteProduct_existingProduct_callsRepositoryDelete() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, "To Delete");
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        productService.deleteProduct(id);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_notFound_throwsRuntimeException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    // ── getProductById ────────────────────────────────────────

    @Test
    void getProductById_found_returnsDTO() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, "Found Product");
        when(productRepository.findByIdWithDetails(id)).thenReturn(product);

        ProductDTO result = productService.getProductById(id);

        assertThat(result.getName()).isEqualTo("Found Product");
    }

    @Test
    void getProductById_notFound_throwsRuntimeException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findByIdWithDetails(id)).thenReturn(null);

        assertThatThrownBy(() -> productService.getProductById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    // ── searchProducts ────────────────────────────────────────

    @Test
    void searchProducts_noFilters_returnsAllProducts() {
        List<Product> products = List.of(
                buildProduct(UUID.randomUUID(), "Hoodie"),
                buildProduct(UUID.randomUUID(), "Sneakers")
        );
        when(productRepository.findAllWithDetails()).thenReturn(products);

        List<ProductDTO> result = productService.searchProducts(new SearchRequest(), null);

        assertThat(result).hasSize(2);
    }

    @Test
    void searchProducts_noPriceFilter_includesProductsWithNoPrices() {
        // Regression test: newly created products (no store prices) must appear
        // when no price filter is active.
        Product noPriceProduct = buildProduct(UUID.randomUUID(), "New Product");
        noPriceProduct.setPrices(Collections.emptyList());
        when(productRepository.findAllWithDetails()).thenReturn(List.of(noPriceProduct));

        List<ProductDTO> result = productService.searchProducts(new SearchRequest(), null);

        assertThat(result).hasSize(1);
    }

    @Test
    void searchProducts_minPriceFilter_excludesProductsWithNoPrices() {
        Product noPriceProduct = buildProduct(UUID.randomUUID(), "No Price");
        noPriceProduct.setPrices(Collections.emptyList());
        when(productRepository.findAllWithDetails()).thenReturn(List.of(noPriceProduct));

        SearchRequest request = new SearchRequest();
        request.setMinPrice(new BigDecimal("10.00"));

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result).isEmpty();
    }

    @Test
    void searchProducts_priceRange_keepsProductsWithinRange() {
        Product inRange = buildProductWithPrice(UUID.randomUUID(), "In Range", new BigDecimal("50.00"));
        Product outRange = buildProductWithPrice(UUID.randomUUID(), "Out Range", new BigDecimal("200.00"));
        when(productRepository.findAllWithDetails()).thenReturn(List.of(inRange, outRange));

        SearchRequest request = new SearchRequest();
        request.setMinPrice(new BigDecimal("10.00"));
        request.setMaxPrice(new BigDecimal("100.00"));

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("In Range");
    }

    @Test
    void searchProducts_withQueryString_usesSearchRepository() {
        List<Product> found = List.of(buildProduct(UUID.randomUUID(), "Hoodie"));
        when(productRepository.searchProducts("hoodie")).thenReturn(found);
        when(searchHistoryRepository.save(any())).thenReturn(null);

        SearchRequest request = new SearchRequest();
        request.setQuery("hoodie");

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result).hasSize(1);
        verify(productRepository).searchProducts("hoodie");
        verify(productRepository, never()).findAllWithDetails();
    }

    @Test
    void searchProducts_brandFilter_excludesOtherBrands() {
        UUID nikeId = UUID.randomUUID();
        Brand nike = new Brand(); nike.setId(nikeId); nike.setName("Nike");
        Brand adidas = new Brand(); adidas.setId(UUID.randomUUID()); adidas.setName("Adidas");

        Product nikeProduct = buildProduct(UUID.randomUUID(), "Nike Shoe");
        nikeProduct.setBrand(nike);
        Product adidasProduct = buildProduct(UUID.randomUUID(), "Adidas Shoe");
        adidasProduct.setBrand(adidas);

        when(productRepository.findAllWithDetails()).thenReturn(List.of(nikeProduct, adidasProduct));

        SearchRequest request = new SearchRequest();
        request.setBrandIds(List.of(nikeId));

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Nike Shoe");
    }
}
