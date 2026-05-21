package com.allinoneshop.repository;

import com.allinoneshop.entity.SearchHistory;

import java.util.List;
import java.util.UUID;

public interface SearchHistoryRepository {

    SearchHistory save(SearchHistory history);

    List<String> findRecentSearchesByUserId(UUID userId, int limit);

    List<String> findTrendingSearches(int limit);

    void deleteByUserId(UUID userId);

    long count();
}
