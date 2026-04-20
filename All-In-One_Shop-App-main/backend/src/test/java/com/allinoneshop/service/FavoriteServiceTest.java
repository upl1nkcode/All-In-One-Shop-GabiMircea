package com.allinoneshop.service;

import com.allinoneshop.dto.ProductDTO;
import com.allinoneshop.entity.Product;
import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Gender;
import com.allinoneshop.entity.enums.Role;
import com.allinoneshop.repository.FavoriteRepository;
import com.allinoneshop.repository.ProductRepository;
import com.allinoneshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class FavoriteServiceTest {

    private FavoriteRepository favoriteRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;
    private FavoriteService favoriteService;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        favoriteRepository = new FavoriteRepository();
        productRepository = new ProductRepository();
        userRepository = new UserRepository();
        favoriteService = new FavoriteService(favoriteRepository, productRepository, userRepository);

        testUser = userRepository.save(User.builder()
                .email("user@test.com").passwordHash("h").role(Role.USER).build());
        testProduct = productRepository.save(Product.builder()
                .name("Test Product").isActive(true).gender(Gender.UNISEX)
                .prices(new ArrayList<>()).build());
    }

    @Test
    void addFavorite_addsSuccessfully() {
        favoriteService.addFavorite(testUser.getId(), testProduct.getId());

        assertThat(favoriteRepository.count()).isEqualTo(1);
        assertThat(favoriteService.isFavorite(testUser.getId(), testProduct.getId())).isTrue();
    }

    @Test
    void addFavorite_duplicate_throwsException() {
        favoriteService.addFavorite(testUser.getId(), testProduct.getId());

        assertThatThrownBy(() -> favoriteService.addFavorite(testUser.getId(), testProduct.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already in favorites");
    }

    @Test
    void addFavorite_userNotFound_throwsException() {
        assertThatThrownBy(() -> favoriteService.addFavorite(UUID.randomUUID(), testProduct.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void addFavorite_productNotFound_throwsException() {
        assertThatThrownBy(() -> favoriteService.addFavorite(testUser.getId(), UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void removeFavorite_removesSuccessfully() {
        favoriteService.addFavorite(testUser.getId(), testProduct.getId());
        favoriteService.removeFavorite(testUser.getId(), testProduct.getId());

        assertThat(favoriteService.isFavorite(testUser.getId(), testProduct.getId())).isFalse();
    }

    @Test
    void isFavorite_notFavorited_returnsFalse() {
        assertThat(favoriteService.isFavorite(testUser.getId(), testProduct.getId())).isFalse();
    }

    @Test
    void getUserFavorites_returnsProductDTOs() {
        favoriteService.addFavorite(testUser.getId(), testProduct.getId());

        List<ProductDTO> result = favoriteService.getUserFavorites(testUser.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Product");
    }

    @Test
    void getFavoriteProductIds_returnsIds() {
        Product p2 = productRepository.save(Product.builder()
                .name("Product 2").isActive(true).gender(Gender.UNISEX)
                .prices(new ArrayList<>()).build());

        favoriteService.addFavorite(testUser.getId(), testProduct.getId());
        favoriteService.addFavorite(testUser.getId(), p2.getId());

        Set<UUID> ids = favoriteService.getFavoriteProductIds(testUser.getId());

        assertThat(ids).hasSize(2);
        assertThat(ids).contains(testProduct.getId(), p2.getId());
    }

    @Test
    void getUserFavorites_emptyList() {
        List<ProductDTO> result = favoriteService.getUserFavorites(testUser.getId());
        assertThat(result).isEmpty();
    }
}
