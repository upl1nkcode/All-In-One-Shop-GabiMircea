package com.allinoneshop.service;

import com.allinoneshop.dto.BrandDTO;
import com.allinoneshop.dto.CategoryDTO;
import com.allinoneshop.dto.ProductDTO;
import com.allinoneshop.entity.Brand;
import com.allinoneshop.entity.Category;
import com.allinoneshop.entity.Product;
import com.allinoneshop.entity.ProductPrice;
import com.allinoneshop.entity.Store;
import com.allinoneshop.entity.enums.Gender;
import com.allinoneshop.repository.BrandRepository;
import com.allinoneshop.repository.CategoryRepository;
import com.allinoneshop.repository.ProductPriceRepository;
import com.allinoneshop.repository.ProductRepository;
import com.allinoneshop.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductPriceRepository priceRepository;
    private final MeilisearchService meilisearchService;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productRepository.count());
        stats.put("activeStores", storeRepository.count());
        stats.put("totalBrands", brandRepository.count());
        stats.put("totalPrices", priceRepository.count());
        stats.put("avgPrice", 0);
        stats.put("topPriceProduct", "N/A");
        return stats;
    }

    @Transactional
    public ProductDTO ingestProduct(Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String brandName = (String) payload.get("brand");
        String categoryName = (String) payload.get("category");
        
        // Find or create Brand
        Brand brand = null;
        if (brandName != null && !brandName.isEmpty()) {
            brand = brandRepository.findByName(brandName)
                    .orElseGet(() -> brandRepository.save(Brand.builder().name(brandName).build()));
        }

        // Find or create Category
        Category category = null;
        if (categoryName != null && !categoryName.isEmpty()) {
            category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> categoryRepository.save(Category.builder()
                            .name(categoryName)
                            .slug(categoryName.toLowerCase().replace(" ", "-"))
                            .build()));
        }

        // Deduplication: look for product by name and brand
        Product product = null;
        if (brand != null) {
            List<Product> existing = productRepository.findByBrandName(brand.getName());
            product = existing.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        }

        if (product == null) {
            product = new Product();
            product.setName(name);
            product.setBrand(brand);
            product.setCategory(category);
            product.setPrices(new ArrayList<>());
        }

        product.setDescription((String) payload.get("description"));
        product.setImageUrl((String) payload.get("imageUrl"));
        
        if (payload.get("sizes") instanceof List) {
            List<?> sizes = (List<?>) payload.get("sizes");
            product.setSizes(sizes.stream().map(Object::toString).toArray(String[]::new));
        }
        
        if (payload.get("colors") instanceof List) {
            List<?> colors = (List<?>) payload.get("colors");
            product.setColors(colors.stream().map(Object::toString).toArray(String[]::new));
        }
        
        if (payload.get("gender") != null) {
            try {
                product.setGender(Gender.valueOf(((String) payload.get("gender")).toUpperCase()));
            } catch (Exception e) {
                product.setGender(Gender.UNISEX);
            }
        } else {
            product.setGender(Gender.UNISEX);
        }

        product = productRepository.save(product);

        // Handle price entry
        @SuppressWarnings("unchecked")
        Map<String, Object> priceData = (Map<String, Object>) payload.get("price");
        if (priceData != null) {
            String storeName = (String) priceData.get("storeName");
            Store store = storeRepository.findByName(storeName)
                    .orElseGet(() -> storeRepository.save(Store.builder()
                            .name(storeName)
                            .website("https://" + storeName.toLowerCase() + ".com")
                            .isActive(true)
                            .build()));

            final Product fProduct = product;
            if (product.getPrices() == null) {
                product.setPrices(new ArrayList<>());
            }
            ProductPrice price = product.getPrices().stream()
                    .filter(p -> p.getStore().getId().equals(store.getId()))
                    .findFirst()
                    .orElseGet(() -> {
                        ProductPrice pp = new ProductPrice();
                        pp.setProduct(fProduct);
                        pp.setStore(store);
                        return pp;
                    });

            price.setPrice(new BigDecimal(priceData.get("price").toString()));
            if (priceData.get("originalPrice") != null) {
                price.setOriginalPrice(new BigDecimal(priceData.get("originalPrice").toString()));
            }
            price.setCurrency((String) priceData.get("currency"));
            price.setProductUrl((String) priceData.get("productUrl"));
            price.setInStock((Boolean) priceData.getOrDefault("inStock", true));

            priceRepository.save(price);
            
            // Add to product prices list if not already there to ensure the DTO gets the full mapping
            if (!product.getPrices().contains(price)) {
                product.getPrices().add(price);
            }
        }

        // Create full DTO to sync mapping
        ProductDTO dto = convertProductToDTO(product);

        meilisearchService.indexProduct(dto);

        return dto;
    }

    public Map<String, Object> runScraper() {
        // Trigger the Python scraper service via its HTTP endpoint
        String scraperUrl = System.getenv("SCRAPER_TRIGGER_URL");
        if (scraperUrl == null || scraperUrl.isEmpty()) {
            scraperUrl = "http://scraper:9090/scrape";
        }

        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(scraperUrl))
                    .POST(java.net.http.HttpRequest.BodyPublishers.noBody())
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();
            client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            // Scraper may not be reachable, log and continue
        }

        // Return current stats (scraper posts results asynchronously)
        Map<String, Object> result = new HashMap<>();
        result.put("status", "triggered");
        result.put("totalProducts", productRepository.count());
        result.put("totalStores", storeRepository.count());
        result.put("totalPrices", priceRepository.count());
        return result;
    }

    private ProductDTO convertProductToDTO(Product product) {
        BigDecimal lowestPrice = null;
        BigDecimal highestPrice = null;

        if (product.getPrices() != null && !product.getPrices().isEmpty()) {
            lowestPrice = product.getPrices().stream()
                    .map(ProductPrice::getPrice)
                    .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            highestPrice = product.getPrices().stream()
                    .map(ProductPrice::getPrice)
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        }

        BrandDTO brandDTO = product.getBrand() != null ?
                BrandDTO.builder().id(product.getBrand().getId()).name(product.getBrand().getName()).build() : null;
        CategoryDTO categoryDTO = product.getCategory() != null ?
                CategoryDTO.builder().id(product.getCategory().getId()).name(product.getCategory().getName())
                        .slug(product.getCategory().getSlug()).build() : null;

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .brand(brandDTO)
                .category(categoryDTO)
                .sizes(product.getSizes())
                .colors(product.getColors())
                .lowestPrice(lowestPrice)
                .highestPrice(highestPrice)
                .storeCount(product.getPrices() != null ? product.getPrices().size() : 0)
                .isActive(product.getIsActive())
                .gender(product.getGender() != null ? product.getGender().name() : null)
                .build();
    }
}
