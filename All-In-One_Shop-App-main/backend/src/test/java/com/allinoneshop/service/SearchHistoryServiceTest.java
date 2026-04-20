package com.allinoneshop.service;

import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import com.allinoneshop.repository.SearchHistoryRepository;
import com.allinoneshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SearchHistoryServiceTest {

    private SearchHistoryRepository searchHistoryRepository;
    private UserRepository userRepository;
    private SearchHistoryService searchHistoryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        searchHistoryRepository = new SearchHistoryRepository();
        userRepository = new UserRepository();
        searchHistoryService = new SearchHistoryService(searchHistoryRepository, userRepository);

        testUser = userRepository.save(User.builder()
                .email("user@test.com").passwordHash("h").role(Role.USER).build());
    }

    @Test
    void saveSearch_savesSuccessfully() {
        searchHistoryService.saveSearch("hoodie", 5, testUser.getId());

        assertThat(searchHistoryRepository.count()).isEqualTo(1);
    }

    @Test
    void saveSearch_withoutUser_savesSuccessfully() {
        searchHistoryService.saveSearch("shoes", 10, null);

        assertThat(searchHistoryRepository.count()).isEqualTo(1);
    }

    @Test
    void getRecentSearches_returnsUserSearches() {
        searchHistoryService.saveSearch("hoodie", 5, testUser.getId());
        searchHistoryService.saveSearch("sneakers", 10, testUser.getId());

        List<String> result = searchHistoryService.getRecentSearches(testUser.getId(), 10);

        assertThat(result).hasSize(2);
        assertThat(result).contains("hoodie", "sneakers");
    }

    @Test
    void getRecentSearches_respectsLimit() {
        for (int i = 0; i < 20; i++) {
            searchHistoryService.saveSearch("query" + i, i, testUser.getId());
        }

        List<String> result = searchHistoryService.getRecentSearches(testUser.getId(), 5);

        assertThat(result).hasSize(5);
    }

    @Test
    void getTrendingSearches_returnsByFrequency() {
        searchHistoryService.saveSearch("popular", 10, null);
        searchHistoryService.saveSearch("popular", 10, null);
        searchHistoryService.saveSearch("popular", 10, null);
        searchHistoryService.saveSearch("rare", 2, null);

        List<String> result = searchHistoryService.getTrendingSearches(10);

        assertThat(result.get(0)).isEqualTo("popular");
    }

    @Test
    void clearUserHistory_removesAllUserSearches() {
        searchHistoryService.saveSearch("q1", 1, testUser.getId());
        searchHistoryService.saveSearch("q2", 2, testUser.getId());

        searchHistoryService.clearUserHistory(testUser.getId());

        List<String> result = searchHistoryService.getRecentSearches(testUser.getId(), 10);
        assertThat(result).isEmpty();
    }

    @Test
    void clearUserHistory_doesNotAffectOtherUsers() {
        User otherUser = userRepository.save(User.builder()
                .email("other@test.com").passwordHash("h").role(Role.USER).build());

        searchHistoryService.saveSearch("q1", 1, testUser.getId());
        searchHistoryService.saveSearch("q2", 2, otherUser.getId());

        searchHistoryService.clearUserHistory(testUser.getId());

        assertThat(searchHistoryService.getRecentSearches(otherUser.getId(), 10)).hasSize(1);
    }
}
