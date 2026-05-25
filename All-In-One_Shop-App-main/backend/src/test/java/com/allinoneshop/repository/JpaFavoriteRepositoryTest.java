package com.allinoneshop.repository;

import com.allinoneshop.entity.Favorite;
import com.allinoneshop.entity.Product;
import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Gender;
import com.allinoneshop.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaFavoriteRepositoryTest {

    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("fav-user@test.com")
                .passwordHash("h")
                .role(Role.USER)
                .build());
        product = productRepository.save(Product.builder()
                .name("Test Product")
                .gender(Gender.UNISEX)
                .isActive(true)
                .build());
    }

    @Test
    void save_persistsFavorite() {
        Favorite fav = favoriteRepository.save(
                Favorite.builder().user(user).product(product).build());
        assertThat(fav.getId()).isNotNull();
    }

    @Test
    void findByUserId_returnsUserFavorites() {
        favoriteRepository.save(Favorite.builder().user(user).product(product).build());
        List<Favorite> favs = favoriteRepository.findByUserId(user.getId());
        assertThat(favs).hasSize(1);
        assertThat(favs.get(0).getProduct().getId()).isEqualTo(product.getId());
    }

    @Test
    void findByUserId_differentUser_returnsEmpty() {
        User other = userRepository.save(User.builder()
                .email("other@test.com").passwordHash("h").role(Role.USER).build());
        favoriteRepository.save(Favorite.builder().user(other).product(product).build());
        assertThat(favoriteRepository.findByUserId(user.getId())).isEmpty();
    }

    @Test
    void findByUserIdWithProducts_loadsProduct() {
        favoriteRepository.save(Favorite.builder().user(user).product(product).build());
        List<Favorite> favs = favoriteRepository.findByUserIdWithProducts(user.getId());
        assertThat(favs).hasSize(1);
        assertThat(favs.get(0).getProduct().getName()).isEqualTo("Test Product");
    }

    @Test
    void existsByUserIdAndProductId_true_whenFavoriteExists() {
        favoriteRepository.save(Favorite.builder().user(user).product(product).build());
        assertThat(favoriteRepository.existsByUserIdAndProductId(user.getId(), product.getId())).isTrue();
    }

    @Test
    void existsByUserIdAndProductId_false_whenFavoriteAbsent() {
        assertThat(favoriteRepository.existsByUserIdAndProductId(user.getId(), product.getId())).isFalse();
    }

    @Test
    void deleteByUserIdAndProductId_removesFavorite() {
        favoriteRepository.save(Favorite.builder().user(user).product(product).build());
        favoriteRepository.deleteByUserIdAndProductId(user.getId(), product.getId());
        assertThat(favoriteRepository.findByUserId(user.getId())).isEmpty();
    }

    @Test
    void count_reflectsSavedFavorites() {
        assertThat(favoriteRepository.count()).isZero();
        favoriteRepository.save(Favorite.builder().user(user).product(product).build());
        assertThat(favoriteRepository.count()).isEqualTo(1);
    }
}
