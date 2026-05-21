package com.allinoneshop.repository;

import com.allinoneshop.entity.SearchHistory;
import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class InMemorySearchHistoryRepositoryTest {

    private InMemorySearchHistoryRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemorySearchHistoryRepository();
    }

    private User buildUser() {
        return User.builder().id(UUID.randomUUID()).email("user@test.com")
                .passwordHash("h").role(Role.USER).build();
    }

    private SearchHistory buildHistory(String query, User user) {
        return SearchHistory.builder().searchQuery(query).resultsCount(5).user(user).build();
    }

    @Test
    void save_generatesId() {
        SearchHistory h = repo.save(buildHistory("hoodie", buildUser()));
        assertThat(h.getId()).isNotNull();
    }

    @Test
    void save_setsCreatedAt() {
        SearchHistory h = repo.save(buildHistory("sneakers", buildUser()));
        assertThat(h.getCreatedAt()).isNotNull();
    }

    @Test
    void findRecentSearchesByUserId_returnsDistinctSortedByRecent() throws InterruptedException {
        User user = buildUser();
        repo.save(buildHistory("shoes", user));
        Thread.sleep(10);
        repo.save(buildHistory("bags", user));
        Thread.sleep(10);
        repo.save(buildHistory("shoes", user)); // duplicate

        List<String> recent = repo.findRecentSearchesByUserId(user.getId(), 10);
        assertThat(recent).containsExactly("shoes", "bags"); // shoes is most recent due to duplicate
    }

    @Test
    void findRecentSearchesByUserId_respectsLimit() {
        User user = buildUser();
        for (int i = 0; i < 5; i++) {
            repo.save(buildHistory("query" + i, user));
        }

        List<String> recent = repo.findRecentSearchesByUserId(user.getId(), 3);
        assertThat(recent).hasSize(3);
    }

    @Test
    void findRecentSearchesByUserId_onlyReturnsOwnHistory() {
        User u1 = buildUser();
        User u2 = User.builder().id(UUID.randomUUID()).email("u2@test.com")
                .passwordHash("h").role(Role.USER).build();

        repo.save(buildHistory("boots", u1));
        repo.save(buildHistory("hats", u2));

        assertThat(repo.findRecentSearchesByUserId(u1.getId(), 10))
                .containsExactly("boots");
    }

    @Test
    void findTrendingSearches_sortsByFrequency() {
        for (int i = 0; i < 3; i++) repo.save(buildHistory("hoodie", null));
        for (int i = 0; i < 5; i++) repo.save(buildHistory("sneakers", null));
        for (int i = 0; i < 1; i++) repo.save(buildHistory("jeans", null));

        List<String> trending = repo.findTrendingSearches(10);
        assertThat(trending.get(0)).isEqualTo("sneakers");
        assertThat(trending.get(1)).isEqualTo("hoodie");
        assertThat(trending.get(2)).isEqualTo("jeans");
    }

    @Test
    void findTrendingSearches_respectsLimit() {
        repo.save(buildHistory("a", null));
        repo.save(buildHistory("b", null));
        repo.save(buildHistory("c", null));

        assertThat(repo.findTrendingSearches(2)).hasSize(2);
    }

    @Test
    void deleteByUserId_removesOnlyUserHistory() {
        User u1 = buildUser();
        User u2 = User.builder().id(UUID.randomUUID()).email("other@test.com")
                .passwordHash("h").role(Role.USER).build();

        repo.save(buildHistory("query1", u1));
        repo.save(buildHistory("query2", u1));
        repo.save(buildHistory("query3", u2));

        repo.deleteByUserId(u1.getId());

        assertThat(repo.findRecentSearchesByUserId(u1.getId(), 10)).isEmpty();
        assertThat(repo.findRecentSearchesByUserId(u2.getId(), 10)).hasSize(1);
    }

    @Test
    void count_reflectsStoredHistory() {
        assertThat(repo.count()).isZero();
        repo.save(buildHistory("test", null));
        repo.save(buildHistory("test2", null));
        assertThat(repo.count()).isEqualTo(2);
    }
}
