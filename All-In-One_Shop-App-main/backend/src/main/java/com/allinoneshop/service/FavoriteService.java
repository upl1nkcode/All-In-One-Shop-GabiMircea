package com.allinoneshop.service;

import com.allinoneshop.dto.*;
import com.allinoneshop.entity.*;
import com.allinoneshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ProductDTO> getUserFavorites(UUID userId) {
        return favoriteRepository.findByUserIdWithProducts(userId).stream()
                .map(favorite -> convertToDTO(favorite.getProduct()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFavorite(UUID userId, UUID productId) {
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("Product already in favorites");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Favorite favorite = Favorite.builder()
                .user(user)
                .product(product)
                .build();

        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(UUID userId, UUID productId) {
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(UUID userId, UUID productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Transactional(readOnly = true)
    public Set<UUID> getFavoriteProductIds(UUID userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(f -> f.getProduct().getId())
                .collect(Collectors.toSet());
    }

    private ProductDTO convertToDTO(Product product) {
        List<ProductPriceDTO> priceDTOs = new ArrayList<>();
        BigDecimal lowestPrice = null;
        BigDecimal highestPrice = null;

        if (product.getPrices() != null && !product.getPrices().isEmpty()) {
            priceDTOs = product.getPrices().stream()
                    .map(this::convertPriceToDTO)
                    .sorted(Comparator.comparing(ProductPriceDTO::getPrice))
                    .collect(Collectors.toList());

            lowestPrice = priceDTOs.get(0).getPrice();
            highestPrice = priceDTOs.get(priceDTOs.size() - 1).getPrice();
        }

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .additionalImages(product.getAdditionalImages())
                .sizes(product.getSizes())
                .colors(product.getColors())
                .brand(product.getBrand() != null ? BrandDTO.builder()
                        .id(product.getBrand().getId())
                        .name(product.getBrand().getName())
                        .logoUrl(product.getBrand().getLogoUrl())
                        .build() : null)
                .category(product.getCategory() != null ? CategoryDTO.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .slug(product.getCategory().getSlug())
                        .build() : null)
                .prices(priceDTOs)
                .lowestPrice(lowestPrice)
                .highestPrice(highestPrice)
                .storeCount(priceDTOs.size())
                .build();
    }

    private ProductPriceDTO convertPriceToDTO(ProductPrice price) {
        BigDecimal savings = null;
        if (price.getOriginalPrice() != null) {
            savings = price.getOriginalPrice().subtract(price.getPrice());
        }

        return ProductPriceDTO.builder()
                .id(price.getId())
                .store(StoreDTO.builder()
                        .id(price.getStore().getId())
                        .name(price.getStore().getName())
                        .website(price.getStore().getWebsite())
                        .logoUrl(price.getStore().getLogoUrl())
                        .build())
                .price(price.getPrice())
                .originalPrice(price.getOriginalPrice())
                .currency(price.getCurrency())
                .productUrl(price.getProductUrl())
                .inStock(price.getInStock())
                .savings(savings)
                .build();
    }
}
