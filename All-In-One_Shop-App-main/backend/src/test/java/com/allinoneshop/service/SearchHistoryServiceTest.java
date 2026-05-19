package com.allinoneshop.service;

import com.allinoneshop.entity.SearchHistory;
import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import com.allinoneshop.repository.SearchHistoryRepository;
import com.allinoneshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchHistoryServiceTest {

    @Mock private SearchHistoryRepository searchHistoryRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private SearchHistoryService searchHistoryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(UUID.randomUUID())
                .email("user@test.com").passwordHash("h").role(Role.USER).build();
    }

    @Test
    void saveSearch_savesSuccessfully() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(searchHistoryRepository.save(any(SearchHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        searchHistoryService.saveSearch("hoodie", 5, testUser.getId());
        verify(searchHistoryRepository).save(any(SearchHistory.class));
    }

    @Test
    void saveSearch_withoutUser_savesSuccessfully() {
        when(searchHistoryRepository.save(any(SearchHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        searchHistoryService.saveSearch("shoes", 10, null);
        verify(searchHistoryRepository).save(any(SearchHistory.class));
    }

    @Test
    void getRecentSearches_returnsUserSearches() {
        when(searchHistoryRepository.findRecentSearchesByUserId(testUser.getId(), 10))
                .thenReturn(List.of("hoodie", "sneakers"));
        List<String> result = searchHistoryService.getRecentSearches(testUser.getId(), 10);
        assertThat(result).hasSize(2).contains("hoodie", "sneakers");
    }

    @Test
    void getTrendingSearches_returnsByFrequency() {
        when(searchHistoryRepository.findTrendingSearches(10)).thenReturn(List.of("popular", "rare"));
        List<String> result = searchHistoryService.getTrendingSearches(10);
        assertThat(result.get(0)).isEqualTo("popular");
    }

    @Test
    void clearUserHistory_removesAllUserSearches() {
        searchHistoryService.clearUserHistory(testUser.getId());
        verify(searchHistoryRepository).deleteByUserId(testUser.getId());
    }
}
