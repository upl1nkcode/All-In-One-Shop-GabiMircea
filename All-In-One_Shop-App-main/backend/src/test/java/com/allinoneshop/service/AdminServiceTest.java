package com.allinoneshop.service;

import com.allinoneshop.dto.ProductDTO;
import com.allinoneshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class AdminServiceTest {

    private ProductRepository productRepository;
    private StoreRepository storeRepository;
    private BrandRepository brandRepository;
    private CategoryRepository categoryRepository;
    private ProductPriceRepository priceRepository;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        productRepository = new ProductRepository();
        storeRepository = new StoreRepository();
        brandRepository = new BrandRepository();
        categoryRepository = new CategoryRepository();
        priceRepository = new ProductPriceRepository();
        adminService = new AdminService(productRepository, storeRepository, brandRepository,
                categoryRepository, priceRepository);
    }

    @Test
    void getDashboardStats_emptyData_returnsZeros() {
        Map<String, Object> stats = adminService.getDashboardStats();

        assertThat(stats.get("totalProducts")).isEqualTo(0L);
        assertThat(stats.get("activeStores")).isEqualTo(0L);
        assertThat(stats.get("totalBrands")).isEqualTo(0L);
        assertThat(stats.get("totalPrices")).isEqualTo(0L);
    }

    @Test
    void ingestProduct_createsProductWithBrandAndCategory() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Test Hoodie");
        payload.put("brand", "Nike");
        payload.put("category", "Hoodies");
        payload.put("description", "A nice hoodie");
        payload.put("gender", "UNISEX");

        ProductDTO result = adminService.ingestProduct(payload);

        assertThat(result.getName()).isEqualTo("Test Hoodie");
        assertThat(result.getBrand().getName()).isEqualTo("Nike");
        assertThat(result.getCategory().getName()).isEqualTo("Hoodies");
        assertThat(productRepository.count()).isEqualTo(1);
        assertThat(brandRepository.count()).isEqualTo(1);
        assertThat(categoryRepository.count()).isEqualTo(1);
    }

    @Test
    void ingestProduct_withPrice_createsPriceEntry() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Sneaker");
        payload.put("brand", "Adidas");
        payload.put("category", "Shoes");
        payload.put("gender", "MEN");

        Map<String, Object> priceData = new HashMap<>();
        priceData.put("storeName", "Footlocker");
        priceData.put("price", "99.99");
        priceData.put("originalPrice", "129.99");
        priceData.put("currency", "EUR");
        priceData.put("productUrl", "https://footlocker.com/sneaker");
        priceData.put("inStock", true);
        payload.put("price", priceData);

        ProductDTO result = adminService.ingestProduct(payload);

        assertThat(result.getStoreCount()).isEqualTo(1);
        assertThat(storeRepository.count()).isEqualTo(1);
        assertThat(priceRepository.count()).isEqualTo(1);
    }

    @Test
    void ingestProduct_duplicateByBrandAndName_updatesExisting() {
        Map<String, Object> first = new HashMap<>();
        first.put("name", "Air Max");
        first.put("brand", "Nike");
        first.put("category", "Shoes");
        first.put("description", "v1");
        adminService.ingestProduct(first);

        Map<String, Object> second = new HashMap<>();
        second.put("name", "Air Max");
        second.put("brand", "Nike");
        second.put("category", "Shoes");
        second.put("description", "v2");
        adminService.ingestProduct(second);

        assertThat(productRepository.count()).isEqualTo(1);
    }

    @Test
    void ingestProduct_invalidGender_defaultsToUnisex() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Invalid Gender Product");
        payload.put("gender", "INVALID");

        ProductDTO result = adminService.ingestProduct(payload);

        assertThat(result.getGender()).isEqualTo("UNISEX");
    }

    @Test
    void ingestProduct_withSizesAndColors_setsArrays() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Styled Product");
        payload.put("sizes", List.of("S", "M", "L"));
        payload.put("colors", List.of("Red", "Blue"));

        ProductDTO result = adminService.ingestProduct(payload);

        assertThat(result.getSizes()).containsExactly("S", "M", "L");
        assertThat(result.getColors()).containsExactly("Red", "Blue");
    }

    @Test
    void runScraper_returnsStatsMap() {
        Map<String, Object> result = adminService.runScraper();

        assertThat(result).containsKey("status");
        assertThat(result.get("status")).isEqualTo("triggered");
        assertThat(result).containsKey("totalProducts");
    }

    @Test
    void getDashboardStats_withData_returnsCorrectValues() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Product");
        payload.put("brand", "Brand");

        Map<String, Object> priceData = new HashMap<>();
        priceData.put("storeName", "Store");
        priceData.put("price", "50.00");
        priceData.put("currency", "EUR");
        priceData.put("productUrl", "https://url.com");
        priceData.put("inStock", true);
        payload.put("price", priceData);

        adminService.ingestProduct(payload);

        Map<String, Object> stats = adminService.getDashboardStats();

        assertThat(stats.get("totalProducts")).isEqualTo(1L);
        assertThat(stats.get("activeStores")).isEqualTo(1L);
        assertThat(stats.get("totalBrands")).isEqualTo(1L);
        assertThat(stats.get("totalPrices")).isEqualTo(1L);
        assertThat((double) stats.get("avgPrice")).isEqualTo(50.0);
    }
}
