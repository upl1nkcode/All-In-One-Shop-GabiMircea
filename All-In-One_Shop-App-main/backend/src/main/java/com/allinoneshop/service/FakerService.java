package com.allinoneshop.service;

import com.allinoneshop.dto.ProductDTO;
import com.allinoneshop.entity.*;
import com.allinoneshop.entity.enums.Gender;
import com.allinoneshop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class FakerService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final ProductPriceRepository priceRepository;
    private final ProductService productService;
    private final SimpMessagingTemplate messagingTemplate;

    private final AtomicBoolean generating = new AtomicBoolean(false);
    private final Faker faker = new Faker();

    public boolean isGenerating() {
        return generating.get();
    }

    public void startGenerating(int intervalMs, int batchSize) {
        if (generating.getAndSet(true)) {
            throw new RuntimeException("Generation already in progress");
        }

        Thread generatorThread = new Thread(() -> {
            log.info("Faker generation started: interval={}ms, batchSize={}", intervalMs, batchSize);
            try {
                while (generating.get()) {
                    List<ProductDTO> generated = new ArrayList<>();
                    for (int i = 0; i < batchSize; i++) {
                        try {
                            ProductDTO dto = generateFakeProduct();
                            generated.add(dto);
                        } catch (Exception e) {
                            log.warn("Failed to generate fake product: {}", e.getMessage());
                        }
                    }

                    Map<String, Object> message = new HashMap<>();
                    message.put("type", "BATCH_ADDED");
                    message.put("count", generated.size());
                    message.put("products", generated);
                    message.put("totalProducts", productRepository.count());
                    message.put("timestamp", System.currentTimeMillis());

                    messagingTemplate.convertAndSend("/topic/products", message);
                    log.debug("Generated and sent batch of {} products", generated.size());

                    Thread.sleep(intervalMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Faker generation interrupted");
            } finally {
                generating.set(false);
                Map<String, Object> stopMessage = new HashMap<>();
                stopMessage.put("type", "GENERATION_STOPPED");
                stopMessage.put("totalProducts", productRepository.count());
                messagingTemplate.convertAndSend("/topic/products", stopMessage);
                log.info("Faker generation stopped");
            }
        });
        generatorThread.setDaemon(true);
        generatorThread.setName("faker-generator");
        generatorThread.start();
    }

    public void stopGenerating() {
        generating.set(false);
    }

    protected ProductDTO generateFakeProduct() {
        // Find or create a brand
        String brandName = faker.company().name();
        Brand brand = brandRepository.findByName(brandName)
                .orElseGet(() -> brandRepository.save(Brand.builder().name(brandName).build()));

        // Find or create a category
        String categoryName = faker.commerce().department();
        Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(categoryName)
                        .slug(categoryName.toLowerCase().replace(" ", "-").replace("&", "and"))
                        .build()));

        // Find or create a store
        String storeName = faker.company().name() + " Store";
        Store store = storeRepository.findByName(storeName)
                .orElseGet(() -> storeRepository.save(Store.builder()
                        .name(storeName)
                        .website("https://" + storeName.toLowerCase().replace(" ", "").replace("'", "") + ".com")
                        .isActive(true)
                        .build()));

        // Create product
        Gender gender = Gender.values()[faker.random().nextInt(Gender.values().length)];
        String productName = faker.commerce().productName();

        Product product = Product.builder()
                .name(productName)
                .description(faker.lorem().paragraph(2))
                .brand(brand)
                .category(category)
                .imageUrl("https://picsum.photos/seed/" + UUID.randomUUID().toString().substring(0, 8) + "/400/400")
                .gender(gender)
                .isActive(true)
                .prices(new ArrayList<>())
                .build();
        product.setSizes(new String[]{"S", "M", "L", "XL"});
        product.setColors(new String[]{faker.color().name(), faker.color().name()});
        product = productRepository.save(product);

        // Create price
        BigDecimal price = BigDecimal.valueOf(faker.number().randomDouble(2, 10, 500))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal originalPrice = price.multiply(BigDecimal.valueOf(1.0 + faker.random().nextDouble() * 0.5))
                .setScale(2, RoundingMode.HALF_UP);

        ProductPrice productPrice = ProductPrice.builder()
                .product(product)
                .store(store)
                .price(price)
                .originalPrice(originalPrice)
                .currency("EUR")
                .productUrl("https://example.com/products/" + product.getId())
                .inStock(faker.bool().bool())
                .build();
        priceRepository.save(productPrice);
        product.getPrices().add(productPrice);

        return productService.convertToDTO(product);
    }
}
