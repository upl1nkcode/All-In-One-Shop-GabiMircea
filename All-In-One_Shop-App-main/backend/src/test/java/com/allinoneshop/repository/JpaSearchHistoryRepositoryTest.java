package com.allinoneshop.repository;

import com.allinoneshop.entity.SearchHistory;
import com.allinoneshop.entity.User;
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
class JpaSearchHistoryRepositoryTest {

    @Autowired private SearchHistoryRepository searchHistoryRepository;
    @Autowired private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("search-user@test.com")
                .passwordHash("h")
                .role(Role.USER)
                .build());
    }

    private SearchHistory history(String query, User u) {
        return SearchHistory.builder().searchQuery(query).user(u).resultsCount(5).build();
    }

    @Test
    void save_persistsHistory() {
        SearchHistory saved = searchHistoryRepository.save(history("shoes", user));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findRecentSearchesByUserId_returnsQueriesForUser() {
        searchHistoryRepository.save(history("shoes", user));
        searchHistoryRepository.save(history("sneakers", user));
        List<String> recent = searchHistoryRepository.findRecentSearchesByUserId(user.getId(), 10);
        assertThat(recent).containsExactlyInAnyOrder("shoes", "sneakers");
    }

    @Test
    void findRecentSearchesByUserId_respectsLimit() {
        for (int i = 0; i < 5; i++) {
            searchHistoryRepository.save(history("query" + i, user));
        }
        List<String> recent = searchHistoryRepository.findRecentSearchesByUserId(user.getId(), 3);
        assertThat(recent).hasSize(3);
    }

    @Test
    void findRecentSearchesByUserId_differentUser_returnsEmpty() {
        User other = userRepository.save(User.builder()
                .email("other@test.com").passwordHash("h").role(Role.USER).build());
        searchHistoryRepository.save(history("boots", other));
        assertThat(searchHistoryRepository.findRecentSearchesByUserId(user.getId(), 10)).isEmpty();
    }

    @Test
    void findTrendingSearches_ordersByFrequency() {
        searchHistoryRepository.save(history("nike", user));
        searchHistoryRepository.save(history("nike", user));
        searchHistoryRepository.save(history("adidas", user));
        List<String> trending = searchHistoryRepository.findTrendingSearches(10);
        assertThat(trending.get(0)).isEqualTo("nike");
    }

    @Test
    void findTrendingSearches_respectsLimit() {
        for (int i = 0; i < 5; i++) {
            searchHistoryRepository.save(history("term" + i, user));
        }
        assertThat(searchHistoryRepository.findTrendingSearches(3)).hasSize(3);
    }

    @Test
    void deleteByUserId_removesAllUserHistory() {
        searchHistoryRepository.save(history("q1", user));
        searchHistoryRepository.save(history("q2", user));
        searchHistoryRepository.deleteByUserId(user.getId());
        assertThat(searchHistoryRepository.findRecentSearchesByUserId(user.getId(), 10)).isEmpty();
    }

    @Test
    void deleteByUserId_doesNotAffectOtherUsers() {
        User other = userRepository.save(User.builder()
                .email("other2@test.com").passwordHash("h").role(Role.USER).build());
        searchHistoryRepository.save(history("kept", other));
        searchHistoryRepository.save(history("removed", user));
        searchHistoryRepository.deleteByUserId(user.getId());
        assertThat(searchHistoryRepository.findRecentSearchesByUserId(other.getId(), 10))
                .containsExactly("kept");
    }

    @Test
    void count_reflectsTotal() {
        assertThat(searchHistoryRepository.count()).isZero();
        searchHistoryRepository.save(history("a", user));
        searchHistoryRepository.save(history("b", user));
        assertThat(searchHistoryRepository.count()).isEqualTo(2);
    }
}
