package com.allinoneshop.service;

import com.allinoneshop.dto.ProductDTO;
import com.allinoneshop.entity.*;
import com.allinoneshop.entity.enums.Gender;
import com.allinoneshop.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductPriceRepository priceRepository;
    @InjectMocks private AdminService adminService;

    @Test
    void getDashboardStats_emptyData_returnsZeros() {
        when(productRepository.count()).thenReturn(0L);
        when(storeRepository.count()).thenReturn(0L);
        when(brandRepository.count()).thenReturn(0L);
        when(priceRepository.count()).thenReturn(0L);
        when(priceRepository.findAll()).thenReturn(List.of());

        Map<String, Object> stats = adminService.getDashboardStats();
        assertThat(stats.get("totalProducts")).isEqualTo(0L);
        assertThat(stats.get("activeStores")).isEqualTo(0L);
        assertThat(stats.get("totalBrands")).isEqualTo(0L);
        assertThat(stats.get("totalPrices")).isEqualTo(0L);
    }

    @Test
    void ingestProduct_createsProductWithBrandAndCategory() {
        Brand brand = Brand.builder().id(UUID.randomUUID()).name("Nike").build();
        Category cat = Category.builder().id(UUID.randomUUID()).name("Hoodies").slug("hoodies").build();

        when(brandRepository.findByName("Nike")).thenReturn(Optional.of(brand));
        when(categoryRepository.findByName("Hoodies")).thenReturn(Optional.of(cat));
        when(productRepository.findByBrandName("Nike")).thenReturn(List.of());
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0); p.setId(UUID.randomUUID()); return p;
        });

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
    }

    @Test
    void ingestProduct_withPrice_createsPriceEntry() {
        Brand brand = Brand.builder().id(UUID.randomUUID()).name("Adidas").build();
        Category cat = Category.builder().id(UUID.randomUUID()).name("Shoes").slug("shoes").build();
        Store store = Store.builder().id(UUID.randomUUID()).name("Footlocker").isActive(true).build();

        when(brandRepository.findByName("Adidas")).thenReturn(Optional.of(brand));
        when(categoryRepository.findByName("Shoes")).thenReturn(Optional.of(cat));
        when(productRepository.findByBrandName("Adidas")).thenReturn(List.of());
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0); p.setId(UUID.randomUUID()); return p;
        });
        when(storeRepository.findByName("Footlocker")).thenReturn(Optional.of(store));
        when(priceRepository.findByProductIdAndStoreId(any(), eq(store.getId()))).thenReturn(Optional.empty());
        when(priceRepository.save(any(ProductPrice.class))).thenAnswer(inv -> {
            ProductPrice pp = inv.getArgument(0); pp.setId(UUID.randomUUID()); return pp;
        });

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
        verify(priceRepository).save(any(ProductPrice.class));
    }

    @Test
    void ingestProduct_invalidGender_defaultsToUnisex() {
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0); p.setId(UUID.randomUUID());
            p.setGender(Gender.UNISEX); return p;
        });

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Invalid Gender Product");
        payload.put("gender", "INVALID");

        ProductDTO result = adminService.ingestProduct(payload);
        assertThat(result.getGender()).isEqualTo("UNISEX");
    }

    @Test
    void runScraper_returnsStatsMap() {
        when(productRepository.count()).thenReturn(5L);
        when(storeRepository.count()).thenReturn(2L);
        when(priceRepository.count()).thenReturn(10L);

        Map<String, Object> result = adminService.runScraper();
        assertThat(result.get("status")).isEqualTo("triggered");
        assertThat(result).containsKey("totalProducts");
    }

    @Test
    void getDashboardStats_withData_returnsCorrectValues() {
        when(productRepository.count()).thenReturn(1L);
        when(storeRepository.count()).thenReturn(1L);
        when(brandRepository.count()).thenReturn(1L);
        when(priceRepository.count()).thenReturn(1L);

        Product p = Product.builder().id(UUID.randomUUID()).name("Product").build();
        ProductPrice pp = ProductPrice.builder().id(UUID.randomUUID()).product(p)
                .price(new BigDecimal("50.00")).build();
        when(priceRepository.findAll()).thenReturn(List.of(pp));

        Map<String, Object> stats = adminService.getDashboardStats();
        assertThat(stats.get("totalProducts")).isEqualTo(1L);
        assertThat(stats.get("activeStores")).isEqualTo(1L);
        assertThat((double) stats.get("avgPrice")).isEqualTo(50.0);
    }
}
