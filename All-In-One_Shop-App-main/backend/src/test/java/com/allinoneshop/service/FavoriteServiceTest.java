package com.allinoneshop.service;

import com.allinoneshop.dto.ProductDTO;
import com.allinoneshop.entity.*;
import com.allinoneshop.entity.enums.Gender;
import com.allinoneshop.entity.enums.Role;
import com.allinoneshop.repository.FavoriteRepository;
import com.allinoneshop.repository.ProductRepository;
import com.allinoneshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private FavoriteService favoriteService;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(UUID.randomUUID())
                .email("user@test.com").passwordHash("h").role(Role.USER).build();
        testProduct = Product.builder().id(UUID.randomUUID())
                .name("Test Product").isActive(true).gender(Gender.UNISEX)
                .prices(new ArrayList<>()).build();
    }

    @Test
    void addFavorite_addsSuccessfully() {
        when(favoriteRepository.existsByUserIdAndProductId(testUser.getId(), testProduct.getId())).thenReturn(false);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(inv -> inv.getArgument(0));

        favoriteService.addFavorite(testUser.getId(), testProduct.getId());
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    void addFavorite_duplicate_throwsException() {
        when(favoriteRepository.existsByUserIdAndProductId(testUser.getId(), testProduct.getId())).thenReturn(true);

        assertThatThrownBy(() -> favoriteService.addFavorite(testUser.getId(), testProduct.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already in favorites");
    }

    @Test
    void addFavorite_userNotFound_throwsException() {
        when(favoriteRepository.existsByUserIdAndProductId(any(), any())).thenReturn(false);
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.addFavorite(UUID.randomUUID(), testProduct.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void addFavorite_productNotFound_throwsException() {
        when(favoriteRepository.existsByUserIdAndProductId(any(), any())).thenReturn(false);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.addFavorite(testUser.getId(), UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void removeFavorite_removesSuccessfully() {
        favoriteService.removeFavorite(testUser.getId(), testProduct.getId());
        verify(favoriteRepository).deleteByUserIdAndProductId(testUser.getId(), testProduct.getId());
    }

    @Test
    void isFavorite_notFavorited_returnsFalse() {
        when(favoriteRepository.existsByUserIdAndProductId(any(), any())).thenReturn(false);
        assertThat(favoriteService.isFavorite(testUser.getId(), testProduct.getId())).isFalse();
    }

    @Test
    void getUserFavorites_returnsProductDTOs() {
        Favorite fav = Favorite.builder().id(UUID.randomUUID()).user(testUser).product(testProduct).build();
        when(favoriteRepository.findByUserIdWithProducts(testUser.getId())).thenReturn(List.of(fav));

        List<ProductDTO> result = favoriteService.getUserFavorites(testUser.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Product");
    }

    @Test
    void getFavoriteProductIds_returnsIds() {
        Product p2 = Product.builder().id(UUID.randomUUID()).name("Product 2").build();
        Favorite f1 = Favorite.builder().id(UUID.randomUUID()).user(testUser).product(testProduct).build();
        Favorite f2 = Favorite.builder().id(UUID.randomUUID()).user(testUser).product(p2).build();
        when(favoriteRepository.findByUserId(testUser.getId())).thenReturn(List.of(f1, f2));

        Set<UUID> ids = favoriteService.getFavoriteProductIds(testUser.getId());
        assertThat(ids).hasSize(2);
        assertThat(ids).contains(testProduct.getId(), p2.getId());
    }

    @Test
    void getUserFavorites_emptyList() {
        when(favoriteRepository.findByUserIdWithProducts(testUser.getId())).thenReturn(List.of());
        List<ProductDTO> result = favoriteService.getUserFavorites(testUser.getId());
        assertThat(result).isEmpty();
    }
}
