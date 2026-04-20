package com.allinoneshop.service;

import com.allinoneshop.dto.*;
import com.allinoneshop.entity.*;
import com.allinoneshop.entity.enums.Gender;
import com.allinoneshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private BrandRepository brandRepository;
    private CategoryRepository categoryRepository;
    private SearchHistoryRepository searchHistoryRepository;
    private UserRepository userRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = new ProductRepository();
        brandRepository = new BrandRepository();
        categoryRepository = new CategoryRepository();
        searchHistoryRepository = new SearchHistoryRepository();
        userRepository = new UserRepository();
        productService = new ProductService(
                productRepository, brandRepository, categoryRepository,
                searchHistoryRepository, userRepository);
    }

    // ── helpers ──────────────────────────────────────────────

    private Product saveProduct(String name) {
        Product p = Product.builder()
                .name(name).isActive(true).gender(Gender.UNISEX)
                .prices(new ArrayList<>()).build();
        return productRepository.save(p);
    }

    private Product saveProductWithPrice(String name, BigDecimal price) {
        Product p = saveProduct(name);
        Store store = Store.builder().id(UUID.randomUUID())
                .name("Store").website("https://s.com").isActive(true).build();
        ProductPrice pp = ProductPrice.builder()
                .id(UUID.randomUUID()).product(p).store(store)
                .price(price).currency("EUR").productUrl("https://u.com").inStock(true)
                .build();
        p.getPrices().add(pp);
        return productRepository.save(p);
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
        ProductDTO result = productService.createProduct(buildDto("Classic Hoodie"));

        assertThat(result.getName()).isEqualTo("Classic Hoodie");
        assertThat(result.getId()).isNotNull();
        assertThat(productRepository.count()).isEqualTo(1);
    }

    @Test
    void createProduct_withValidBrandId_setsBrand() {
        Brand brand = brandRepository.save(Brand.builder().name("Nike").build());

        ProductDTO dto = buildDto("Nike Shoe");
        dto.setBrandId(brand.getId());

        ProductDTO result = productService.createProduct(dto);

        assertThat(result.getBrand()).isNotNull();
        assertThat(result.getBrand().getName()).isEqualTo("Nike");
    }

    @Test
    void createProduct_withValidCategoryId_setsCategory() {
        Category cat = categoryRepository.save(Category.builder().name("Sneakers").slug("sneakers").build());

        ProductDTO dto = buildDto("Air Max");
        dto.setCategoryId(cat.getId());

        ProductDTO result = productService.createProduct(dto);

        assertThat(result.getCategory()).isNotNull();
        assertThat(result.getCategory().getName()).isEqualTo("Sneakers");
    }

    @Test
    void createProduct_setsIsActiveTrue() {
        ProductDTO result = productService.createProduct(buildDto("New Product"));
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void createProduct_withSizesAndColors_setsArrays() {
        ProductDTO dto = buildDto("Styled Product");
        dto.setSizes(new String[]{"S", "M", "L"});
        dto.setColors(new String[]{"Red", "Blue"});

        ProductDTO result = productService.createProduct(dto);

        assertThat(result.getSizes()).containsExactly("S", "M", "L");
        assertThat(result.getColors()).containsExactly("Red", "Blue");
    }

    @Test
    void createProduct_invalidGender_defaultsToUnisex() {
        ProductDTO dto = buildDto("Gender Test");
        dto.setGender("INVALID");

        ProductDTO result = productService.createProduct(dto);
        assertThat(result.getGender()).isEqualTo("UNISEX");
    }

    // ── updateProduct ─────────────────────────────────────────

    @Test
    void updateProduct_existingProduct_updatesAndReturnsDTO() {
        Product p = saveProduct("Old Name");

        ProductDTO result = productService.updateProduct(p.getId(), buildDto("New Name"));

        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void updateProduct_notFound_throwsRuntimeException() {
        assertThatThrownBy(() -> productService.updateProduct(UUID.randomUUID(), buildDto("x")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    // ── deleteProduct ─────────────────────────────────────────

    @Test
    void deleteProduct_existingProduct_removesIt() {
        Product p = saveProduct("To Delete");
        productService.deleteProduct(p.getId());
        assertThat(productRepository.count()).isZero();
    }

    @Test
    void deleteProduct_notFound_throwsRuntimeException() {
        assertThatThrownBy(() -> productService.deleteProduct(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    // ── getProductById ────────────────────────────────────────

    @Test
    void getProductById_found_returnsDTO() {
        Product p = saveProduct("Found Product");
        ProductDTO result = productService.getProductById(p.getId());
        assertThat(result.getName()).isEqualTo("Found Product");
    }

    @Test
    void getProductById_notFound_throwsRuntimeException() {
        assertThatThrownBy(() -> productService.getProductById(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    // ── searchProducts ────────────────────────────────────────

    @Test
    void searchProducts_noFilters_returnsAllProducts() {
        saveProduct("Hoodie");
        saveProduct("Sneakers");

        List<ProductDTO> result = productService.searchProducts(new SearchRequest(), null);
        assertThat(result).hasSize(2);
    }

    @Test
    void searchProducts_withQuery_filtersResults() {
        saveProduct("Nike Hoodie");
        saveProduct("Adidas Sneakers");

        SearchRequest request = new SearchRequest();
        request.setQuery("hoodie");

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Nike Hoodie");
    }

    @Test
    void searchProducts_withQuery_savesSearchHistory() {
        saveProduct("Test Product");

        SearchRequest request = new SearchRequest();
        request.setQuery("test");
        productService.searchProducts(request, null);

        assertThat(searchHistoryRepository.count()).isEqualTo(1);
    }

    @Test
    void searchProducts_noPriceFilter_includesProductsWithNoPrices() {
        saveProduct("No Price Product");

        List<ProductDTO> result = productService.searchProducts(new SearchRequest(), null);
        assertThat(result).hasSize(1);
    }

    @Test
    void searchProducts_minPriceFilter_excludesProductsWithNoPrices() {
        saveProduct("No Price");

        SearchRequest request = new SearchRequest();
        request.setMinPrice(new BigDecimal("10.00"));

        List<ProductDTO> result = productService.searchProducts(request, null);
        assertThat(result).isEmpty();
    }

    @Test
    void searchProducts_priceRange_keepsProductsWithinRange() {
        saveProductWithPrice("In Range", new BigDecimal("50.00"));
        saveProductWithPrice("Out Range", new BigDecimal("200.00"));

        SearchRequest request = new SearchRequest();
        request.setMinPrice(new BigDecimal("10.00"));
        request.setMaxPrice(new BigDecimal("100.00"));

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("In Range");
    }

    @Test
    void searchProducts_brandFilter_excludesOtherBrands() {
        Brand nike = brandRepository.save(Brand.builder().name("Nike").build());
        Brand adidas = brandRepository.save(Brand.builder().name("Adidas").build());

        Product p1 = saveProduct("Nike Shoe");
        p1.setBrand(nike);
        productRepository.save(p1);

        Product p2 = saveProduct("Adidas Shoe");
        p2.setBrand(adidas);
        productRepository.save(p2);

        SearchRequest request = new SearchRequest();
        request.setBrandIds(List.of(nike.getId()));

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Nike Shoe");
    }

    @Test
    void searchProducts_categoryFilter() {
        Category cat = categoryRepository.save(Category.builder().name("Shoes").slug("shoes").build());

        Product p1 = saveProduct("Running Shoe");
        p1.setCategory(cat);
        productRepository.save(p1);

        saveProduct("Random");

        SearchRequest request = new SearchRequest();
        request.setCategoryIds(List.of(cat.getId()));

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Running Shoe");
    }

    @Test
    void searchProducts_sizeFilter() {
        Product p = saveProduct("Sized Product");
        p.setSizes(new String[]{"S", "M", "L"});
        productRepository.save(p);

        Product p2 = saveProduct("XL Only");
        p2.setSizes(new String[]{"XL"});
        productRepository.save(p2);

        SearchRequest request = new SearchRequest();
        request.setSizes(List.of("S"));

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Sized Product");
    }

    @Test
    void searchProducts_colorFilter() {
        Product p = saveProduct("Red Product");
        p.setColors(new String[]{"Red", "Blue"});
        productRepository.save(p);

        Product p2 = saveProduct("Green Product");
        p2.setColors(new String[]{"Green"});
        productRepository.save(p2);

        SearchRequest request = new SearchRequest();
        request.setColors(List.of("Red"));

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Red Product");
    }

    @Test
    void searchProducts_sortByPriceAsc() {
        saveProductWithPrice("Expensive", new BigDecimal("100.00"));
        saveProductWithPrice("Cheap", new BigDecimal("10.00"));

        SearchRequest request = new SearchRequest();
        request.setSortBy("price_asc");

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result.get(0).getName()).isEqualTo("Cheap");
        assertThat(result.get(1).getName()).isEqualTo("Expensive");
    }

    @Test
    void searchProducts_sortByPriceDesc() {
        saveProductWithPrice("Expensive", new BigDecimal("100.00"));
        saveProductWithPrice("Cheap", new BigDecimal("10.00"));

        SearchRequest request = new SearchRequest();
        request.setSortBy("price_desc");

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result.get(0).getName()).isEqualTo("Expensive");
    }

    @Test
    void searchProducts_sortByNameAsc() {
        saveProduct("Zebra");
        saveProduct("Alpha");

        SearchRequest request = new SearchRequest();
        request.setSortBy("name_asc");

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result.get(0).getName()).isEqualTo("Alpha");
        assertThat(result.get(1).getName()).isEqualTo("Zebra");
    }

    @Test
    void searchProducts_sortByNameDesc() {
        saveProduct("Alpha");
        saveProduct("Zebra");

        SearchRequest request = new SearchRequest();
        request.setSortBy("name_desc");

        List<ProductDTO> result = productService.searchProducts(request, null);

        assertThat(result.get(0).getName()).isEqualTo("Zebra");
    }

    // ── searchProductsPaged ───────────────────────────────────

    @Test
    void searchProductsPaged_returnsPaginatedResults() {
        for (int i = 0; i < 25; i++) saveProduct("Product " + i);

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
        for (int i = 0; i < 25; i++) saveProduct("Product " + i);

        SearchRequest request = new SearchRequest();
        request.setPage(2);
        request.setSize(10);

        PagedResponse<ProductDTO> result = productService.searchProductsPaged(request, null);

        assertThat(result.getContent()).hasSize(5);
    }

    @Test
    void searchProductsPaged_emptyPage_returnsEmptyContent() {
        for (int i = 0; i < 5; i++) saveProduct("Product " + i);

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
        Category cat = categoryRepository.save(Category.builder().name("Shoes").slug("shoes").build());
        Product p = saveProduct("Running Shoe");
        p.setCategory(cat);
        productRepository.save(p);
        saveProduct("Random Product");

        List<ProductDTO> result = productService.getProductsByCategory("shoes");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Running Shoe");
    }

    @Test
    void getProductsByBrand_returnsCorrectProducts() {
        Brand brand = brandRepository.save(Brand.builder().name("Puma").build());
        Product p = saveProduct("Puma Sneaker");
        p.setBrand(brand);
        productRepository.save(p);
        saveProduct("Other Product");

        List<ProductDTO> result = productService.getProductsByBrand("Puma");
        assertThat(result).hasSize(1);
    }

    @Test
    void getTrendingProducts_returnsLimitedResults() {
        for (int i = 0; i < 20; i++) saveProduct("Product " + i);

        List<ProductDTO> result = productService.getTrendingProducts(5);
        assertThat(result).hasSize(5);
    }

    @Test
    void getSimilarProducts_returnsSameCategoryProducts() {
        Category cat = categoryRepository.save(Category.builder().name("Tops").slug("tops").build());
        Product p1 = saveProduct("T-Shirt"); p1.setCategory(cat); productRepository.save(p1);
        Product p2 = saveProduct("Polo");    p2.setCategory(cat); productRepository.save(p2);
        saveProduct("Unrelated");

        List<ProductDTO> result = productService.getSimilarProducts(p1.getId(), 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Polo");
    }

    @Test
    void getSimilarProducts_productNotFound_returnsEmpty() {
        List<ProductDTO> result = productService.getSimilarProducts(UUID.randomUUID(), 5);
        assertThat(result).isEmpty();
    }
}
