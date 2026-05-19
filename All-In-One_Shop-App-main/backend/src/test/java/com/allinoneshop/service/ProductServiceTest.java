package com.allinoneshop.service;

import com.allinoneshop.dto.*;
import com.allinoneshop.entity.*;
import com.allinoneshop.entity.enums.Gender;
import com.allinoneshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    private Product buildProduct(String name) {
        Product p = Product.builder()
                .id(UUID.randomUUID()).name(name).isActive(true)
                .gender(Gender.UNISEX).prices(new ArrayList<>()).build();
        return p;
    }

    private Product buildProductWithPrice(String name, BigDecimal price) {
        Product p = buildProduct(name);
        Store store = Store.builder().id(UUID.randomUUID())
                .name("Store").website("https://s.com").isActive(true).build();
        ProductPrice pp = ProductPrice.builder()
                .id(UUID.randomUUID()).product(p).store(store)
                .price(price).currency("EUR").productUrl("https://u.com").inStock(true)
                .build();
        p.getPrices().add(pp);
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
        Product saved = buildProduct("Classic Hoodie");
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDTO result = productService.createProduct(buildDto("Classic Hoodie"));

        assertThat(result.getName()).isEqualTo("Classic Hoodie");
        assertThat(result.getId()).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_withValidBrandId_setsBrand() {
        Brand brand = Brand.builder().id(UUID.randomUUID()).name("Nike").build();
        when(brandRepository.findById(brand.getId())).thenReturn(Optional.of(brand));

        Product saved = buildProduct("Nike Shoe");
        saved.setBrand(brand);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDTO dto = buildDto("Nike Shoe");
        dto.setBrandId(brand.getId());
        ProductDTO result = productService.createProduct(dto);

        assertThat(result.getBrand()).isNotNull();
        assertThat(result.getBrand().getName()).isEqualTo("Nike");
    }

    @Test
    void createProduct_withValidCategoryId_setsCategory() {
        Category cat = Category.builder().id(UUID.randomUUID()).name("Sneakers").slug("sneakers").build();
        when(categoryRepository.findById(cat.getId())).thenReturn(Optional.of(cat));

        Product saved = buildProduct("Air Max");
        saved.setCategory(cat);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDTO dto = buildDto("Air Max");
        dto.setCategoryId(cat.getId());
        ProductDTO result = productService.createProduct(dto);

        assertThat(result.getCategory()).isNotNull();
        assertThat(result.getCategory().getName()).isEqualTo("Sneakers");
    }

    @Test
    void createProduct_setsIsActiveTrue() {
        Product saved = buildProduct("New Product");
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDTO result = productService.createProduct(buildDto("New Product"));
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void createProduct_withSizesAndColors_setsArrays() {
        Product saved = buildProduct("Styled Product");
        saved.setSizes(new String[]{"S", "M", "L"});
        saved.setColors(new String[]{"Red", "Blue"});
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDTO dto = buildDto("Styled Product");
        dto.setSizes(new String[]{"S", "M", "L"});
        dto.setColors(new String[]{"Red", "Blue"});
        ProductDTO result = productService.createProduct(dto);

        assertThat(result.getSizes()).containsExactly("S", "M", "L");
        assertThat(result.getColors()).containsExactly("Red", "Blue");
    }

    @Test
    void createProduct_invalidGender_defaultsToUnisex() {
        Product saved = buildProduct("Gender Test");
        saved.setGender(Gender.UNISEX);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDTO dto = buildDto("Gender Test");
        dto.setGender("INVALID");
        ProductDTO result = productService.createProduct(dto);
        assertThat(result.getGender()).isEqualTo("UNISEX");
    }

    // ── updateProduct ─────────────────────────────────────────

    @Test
    void updateProduct_existingProduct_updatesAndReturnsDTO() {
        Product p = buildProduct("Old Name");
        when(productRepository.findById(p.getId())).thenReturn(Optional.of(p));

        Product saved = buildProduct("New Name");
        saved.setId(p.getId());
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDTO result = productService.updateProduct(p.getId(), buildDto("New Name"));
        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void updateProduct_notFound_throwsRuntimeException() {
        when(productRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.updateProduct(UUID.randomUUID(), buildDto("x")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    // ── deleteProduct ─────────────────────────────────────────

    @Test
    void deleteProduct_existingProduct_removesIt() {
        Product p = buildProduct("To Delete");
        when(productRepository.findById(p.getId())).thenReturn(Optional.of(p));

        productService.deleteProduct(p.getId());
        verify(productRepository).delete(p);
    }

    @Test
    void deleteProduct_notFound_throwsRuntimeException() {
        when(productRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.deleteProduct(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    // ── getProductById ────────────────────────────────────────

    @Test
    void getProductById_found_returnsDTO() {
        Product p = buildProduct("Found Product");
        when(productRepository.findByIdWithDetails(p.getId())).thenReturn(p);

        ProductDTO result = productService.getProductById(p.getId());
        assertThat(result.getName()).isEqualTo("Found Product");
    }

    @Test
    void getProductById_notFound_throwsRuntimeException() {
        when(productRepository.findByIdWithDetails(any())).thenReturn(null);
        assertThatThrownBy(() -> productService.getProductById(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    // ── searchProducts ────────────────────────────────────────

    @Test
    void searchProducts_noFilters_returnsAllProducts() {
        when(productRepository.findAllWithDetails())
                .thenReturn(List.of(buildProduct("Hoodie"), buildProduct("Sneakers")));

        List<ProductDTO> result = productService.searchProducts(new SearchRequest(), null);
        assertThat(result).hasSize(2);
    }

    @Test
    void searchProducts_withQuery_filtersResults() {
        when(productRepository.searchProducts("hoodie"))
                .thenReturn(List.of(buildProduct("Nike Hoodie")));

        SearchRequest request = new SearchRequest();
        request.setQuery("hoodie");
        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Nike Hoodie");
    }

    @Test
    void searchProducts_withQuery_savesSearchHistory() {
        when(productRepository.searchProducts("test"))
                .thenReturn(List.of(buildProduct("Test Product")));

        SearchRequest request = new SearchRequest();
        request.setQuery("test");
        productService.searchProducts(request, null);

        verify(searchHistoryRepository).save(any(SearchHistory.class));
    }

    @Test
    void searchProducts_priceRange_keepsProductsWithinRange() {
        when(productRepository.findAllWithDetails()).thenReturn(List.of(
                buildProductWithPrice("In Range", new BigDecimal("50.00")),
                buildProductWithPrice("Out Range", new BigDecimal("200.00"))
        ));

        SearchRequest request = new SearchRequest();
        request.setMinPrice(new BigDecimal("10.00"));
        request.setMaxPrice(new BigDecimal("100.00"));

        List<ProductDTO> result = productService.searchProducts(request, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("In Range");
    }

    @Test
    void searchProducts_brandFilter_excludesOtherBrands() {
        Brand nike = Brand.builder().id(UUID.randomUUID()).name("Nike").build();
        Brand adidas = Brand.builder().id(UUID.randomUUID()).name("Adidas").build();

        Product p1 = buildProduct("Nike Shoe"); p1.setBrand(nike);
        Product p2 = buildProduct("Adidas Shoe"); p2.setBrand(adidas);

        when(productRepository.findAllWithDetails()).thenReturn(List.of(p1, p2));

        SearchRequest request = new SearchRequest();
        request.setBrandIds(List.of(nike.getId()));

        List<ProductDTO> result = productService.searchProducts(request, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Nike Shoe");
    }

    @Test
    void searchProducts_categoryFilter() {
        Category cat = Category.builder().id(UUID.randomUUID()).name("Shoes").slug("shoes").build();
        Product p1 = buildProduct("Running Shoe"); p1.setCategory(cat);
        Product p2 = buildProduct("Random");

        when(productRepository.findAllWithDetails()).thenReturn(List.of(p1, p2));

        SearchRequest request = new SearchRequest();
        request.setCategoryIds(List.of(cat.getId()));

        List<ProductDTO> result = productService.searchProducts(request, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Running Shoe");
    }

    @Test
    void searchProducts_sizeFilter() {
        Product p = buildProduct("Sized Product");
        p.setSizes(new String[]{"S", "M", "L"});
        Product p2 = buildProduct("XL Only");
        p2.setSizes(new String[]{"XL"});

        when(productRepository.findAllWithDetails()).thenReturn(List.of(p, p2));

        SearchRequest request = new SearchRequest();
        request.setSizes(List.of("S"));

        List<ProductDTO> result = productService.searchProducts(request, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Sized Product");
    }

    @Test
    void searchProducts_colorFilter() {
        Product p = buildProduct("Red Product");
        p.setColors(new String[]{"Red", "Blue"});
        Product p2 = buildProduct("Green Product");
        p2.setColors(new String[]{"Green"});

        when(productRepository.findAllWithDetails()).thenReturn(List.of(p, p2));

        SearchRequest request = new SearchRequest();
        request.setColors(List.of("Red"));

        List<ProductDTO> result = productService.searchProducts(request, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Red Product");
    }

    @Test
    void searchProducts_sortByPriceAsc() {
        when(productRepository.findAllWithDetails()).thenReturn(List.of(
                buildProductWithPrice("Expensive", new BigDecimal("100.00")),
                buildProductWithPrice("Cheap", new BigDecimal("10.00"))
        ));

        SearchRequest request = new SearchRequest();
        request.setSortBy("price_asc");

        List<ProductDTO> result = productService.searchProducts(request, null);
        assertThat(result.get(0).getName()).isEqualTo("Cheap");
        assertThat(result.get(1).getName()).isEqualTo("Expensive");
    }

    @Test
    void searchProducts_sortByPriceDesc() {
        when(productRepository.findAllWithDetails()).thenReturn(List.of(
                buildProductWithPrice("Expensive", new BigDecimal("100.00")),
                buildProductWithPrice("Cheap", new BigDecimal("10.00"))
        ));

        SearchRequest request = new SearchRequest();
        request.setSortBy("price_desc");

        List<ProductDTO> result = productService.searchProducts(request, null);
        assertThat(result.get(0).getName()).isEqualTo("Expensive");
    }

    @Test
    void searchProducts_sortByNameAsc() {
        when(productRepository.findAllWithDetails()).thenReturn(List.of(
                buildProduct("Zebra"), buildProduct("Alpha")
        ));

        SearchRequest request = new SearchRequest();
        request.setSortBy("name_asc");

        List<ProductDTO> result = productService.searchProducts(request, null);
        assertThat(result.get(0).getName()).isEqualTo("Alpha");
        assertThat(result.get(1).getName()).isEqualTo("Zebra");
    }

    @Test
    void searchProducts_sortByNameDesc() {
        when(productRepository.findAllWithDetails()).thenReturn(List.of(
                buildProduct("Alpha"), buildProduct("Zebra")
        ));

        SearchRequest request = new SearchRequest();
        request.setSortBy("name_desc");

        List<ProductDTO> result = productService.searchProducts(request, null);
        assertThat(result.get(0).getName()).isEqualTo("Zebra");
    }

    // ── searchProductsPaged ───────────────────────────────────

    @Test
    void searchProductsPaged_returnsPaginatedResults() {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 25; i++) products.add(buildProduct("Product " + i));
        when(productRepository.findAllWithDetails()).thenReturn(products);

        SearchRequest request = new SearchRequest();
        request.setPage(0);
        request.setSize(10);

        PagedResponse<ProductDTO> result = productService.searchProductsPaged(request, null);

        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getPage()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    void searchProductsPaged_lastPage_hasRemainingItems() {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 25; i++) products.add(buildProduct("Product " + i));
        when(productRepository.findAllWithDetails()).thenReturn(products);

        SearchRequest request = new SearchRequest();
        request.setPage(2);
        request.setSize(10);

        PagedResponse<ProductDTO> result = productService.searchProductsPaged(request, null);
        assertThat(result.getContent()).hasSize(5);
    }

    @Test
    void searchProductsPaged_emptyPage_returnsEmptyContent() {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 5; i++) products.add(buildProduct("Product " + i));
        when(productRepository.findAllWithDetails()).thenReturn(products);

        SearchRequest request = new SearchRequest();
        request.setPage(10);
        request.setSize(10);

        PagedResponse<ProductDTO> result = productService.searchProductsPaged(request, null);
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(5);
    }

    // ── getProductsByCategory / Brand / Similar / Trending ────

    @Test
    void getProductsByCategory_returnsCorrectProducts() {
        Category cat = Category.builder().id(UUID.randomUUID()).name("Shoes").slug("shoes").build();
        Product p = buildProduct("Running Shoe"); p.setCategory(cat);

        when(productRepository.findByCategorySlug("shoes")).thenReturn(List.of(p));

        List<ProductDTO> result = productService.getProductsByCategory("shoes");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Running Shoe");
    }

    @Test
    void getProductsByBrand_returnsCorrectProducts() {
        Brand brand = Brand.builder().id(UUID.randomUUID()).name("Puma").build();
        Product p = buildProduct("Puma Sneaker"); p.setBrand(brand);

        when(productRepository.findByBrandName("Puma")).thenReturn(List.of(p));

        List<ProductDTO> result = productService.getProductsByBrand("Puma");
        assertThat(result).hasSize(1);
    }

    @Test
    void getTrendingProducts_returnsLimitedResults() {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 20; i++) products.add(buildProduct("Product " + i));
        when(productRepository.findAllWithDetails()).thenReturn(products);

        List<ProductDTO> result = productService.getTrendingProducts(5);
        assertThat(result).hasSize(5);
    }

    @Test
    void getSimilarProducts_returnsSameCategoryProducts() {
        Category cat = Category.builder().id(UUID.randomUUID()).name("Tops").slug("tops").build();
        Product p1 = buildProduct("T-Shirt"); p1.setCategory(cat);
        Product p2 = buildProduct("Polo"); p2.setCategory(cat);

        when(productRepository.findByIdWithDetails(p1.getId())).thenReturn(p1);
        when(productRepository.findSimilarProducts(cat.getId(), p1.getId(), 10))
                .thenReturn(List.of(p2));

        List<ProductDTO> result = productService.getSimilarProducts(p1.getId(), 10);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Polo");
    }

    @Test
    void getSimilarProducts_productNotFound_returnsEmpty() {
        when(productRepository.findByIdWithDetails(any())).thenReturn(null);

        List<ProductDTO> result = productService.getSimilarProducts(UUID.randomUUID(), 5);
        assertThat(result).isEmpty();
    }
}
