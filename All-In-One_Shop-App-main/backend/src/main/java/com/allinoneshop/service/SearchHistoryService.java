package com.allinoneshop.service;

import com.allinoneshop.entity.SearchHistory;
import com.allinoneshop.repository.SearchHistoryRepository;
import com.allinoneshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<String> getRecentSearches(UUID userId, int limit) {
        return searchHistoryRepository.findRecentSearchesByUserId(userId, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<String> getTrendingSearches(int limit) {
        return searchHistoryRepository.findTrendingSearches(PageRequest.of(0, limit))
                .stream()
                .map(result -> (String) result[0])
                .toList();
    }

    @Transactional
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
        List<SearchHistory> history = searchHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Integer.MAX_VALUE));
        searchHistoryRepository.deleteAll(history);
    }
}
