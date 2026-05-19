package com.allinoneshop.service;

import com.allinoneshop.entity.SearchHistory;
import com.allinoneshop.repository.SearchHistoryRepository;
import com.allinoneshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    public List<String> getRecentSearches(UUID userId, int limit) {
        return searchHistoryRepository.findRecentSearchesByUserId(userId, limit);
    }

    public List<String> getTrendingSearches(int limit) {
        return searchHistoryRepository.findTrendingSearches(limit);
    }

    public void saveSearch(String query, int resultsCount, UUID userId) {
        SearchHistory history = SearchHistory.builder()
                .searchQuery(query)
                .resultsCount(resultsCount)
                .build();

        if (userId != null) {
            userRepository.findById(userId).ifPresent(history::setUser);
        }

        searchHistoryRepository.save(history);
    }

    @Transactional
    public void clearUserHistory(UUID userId) {
        searchHistoryRepository.deleteByUserId(userId);
    }
}
