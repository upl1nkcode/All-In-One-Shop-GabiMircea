package com.allinoneshop.repository;

import com.allinoneshop.entity.*;
import com.allinoneshop.entity.enums.Gender;
import com.allinoneshop.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class InMemoryFavoriteRepositoryTest {

    private InMemoryFavoriteRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryFavoriteRepository();
    }

    private User buildUser(String email) {
        return User.builder().id(UUID.randomUUID()).email(email)
                .passwordHash("h").role(Role.USER).build();
    }

    private Product buildProduct(String name) {
        return Product.builder().id(UUID.randomUUID()).name(name)
                .gender(Gender.UNISEX).isActive(true).build();
    }

    private Favorite buildFavorite(User user, Product product) {
        return Favorite.builder().user(user).product(product).build();
    }

    @Test
    void save_generatesId() {
        Favorite fav = repo.save(buildFavorite(buildUser("a@test.com"), buildProduct("Shoe")));
        assertThat(fav.getId()).isNotNull();
    }

    @Test
    void save_setsCreatedAt() {
        Favorite fav = repo.save(buildFavorite(buildUser("b@test.com"), buildProduct("Jacket")));
        assertThat(fav.getCreatedAt()).isNotNull();
    }

    @Test
    void findByUserId_returnsOnlyUserFavorites() {
        User u1 = buildUser("u1@test.com");
        User u2 = buildUser("u2@test.com");
        Product p1 = buildProduct("P1");
        Product p2 = buildProduct("P2");

        repo.save(buildFavorite(u1, p1));
        repo.save(buildFavorite(u1, p2));
        repo.save(buildFavorite(u2, p1));

        List<Favorite> u1Favs = repo.findByUserId(u1.getId());
        assertThat(u1Favs).hasSize(2);
        assertThat(repo.findByUserId(u2.getId())).hasSize(1);
    }

    @Test
    void existsByUserIdAndProductId_existingPair_returnsTrue() {
        User user = buildUser("user@test.com");
        Product product = buildProduct("Product");
        repo.save(buildFavorite(user, product));

        assertThat(repo.existsByUserIdAndProductId(user.getId(), product.getId())).isTrue();
    }

    @Test
    void existsByUserIdAndProductId_nonExistingPair_returnsFalse() {
        assertThat(repo.existsByUserIdAndProductId(UUID.randomUUID(), UUID.randomUUID())).isFalse();
    }

    @Test
    void deleteByUserIdAndProductId_removesCorrectFavorite() {
        User user = buildUser("del@test.com");
        Product p1 = buildProduct("P1");
        Product p2 = buildProduct("P2");
        repo.save(buildFavorite(user, p1));
        repo.save(buildFavorite(user, p2));

        repo.deleteByUserIdAndProductId(user.getId(), p1.getId());

        assertThat(repo.existsByUserIdAndProductId(user.getId(), p1.getId())).isFalse();
        assertThat(repo.existsByUserIdAndProductId(user.getId(), p2.getId())).isTrue();
        assertThat(repo.count()).isEqualTo(1);
    }

    @Test
    void findByUserIdWithProducts_returnsAllFavorites() {
        User user = buildUser("x@test.com");
        repo.save(buildFavorite(user, buildProduct("A")));
        repo.save(buildFavorite(user, buildProduct("B")));

        assertThat(repo.findByUserIdWithProducts(user.getId())).hasSize(2);
    }

    @Test
    void count_reflectsStoredFavorites() {
        assertThat(repo.count()).isZero();
        repo.save(buildFavorite(buildUser("q@test.com"), buildProduct("Q")));
        assertThat(repo.count()).isEqualTo(1);
    }
}
