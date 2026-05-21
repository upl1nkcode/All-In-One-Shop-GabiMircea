package com.allinoneshop.repository;

import com.allinoneshop.entity.SearchHistory;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemorySearchHistoryRepository implements SearchHistoryRepository {

    private final Map<UUID, SearchHistory> store = new ConcurrentHashMap<>();

    @Override
    public SearchHistory save(SearchHistory history) {
        if (history.getId() == null) history.setId(UUID.randomUUID());
        if (history.getCreatedAt() == null) history.setCreatedAt(OffsetDateTime.now());
        store.put(history.getId(), history);
        return history;
    }

    @Override
    public List<String> findRecentSearchesByUserId(UUID userId, int limit) {
        return store.values().stream()
                .filter(sh -> sh.getUser() != null && userId.equals(sh.getUser().getId()))
                .sorted(Comparator.comparing(SearchHistory::getCreatedAt, Comparator.reverseOrder()))
                .map(SearchHistory::getSearchQuery)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findTrendingSearches(int limit) {
        return store.values().stream()
                .collect(Collectors.groupingBy(SearchHistory::getSearchQuery, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByUserId(UUID userId) {
        store.values().removeIf(sh -> sh.getUser() != null && userId.equals(sh.getUser().getId()));
    }

    @Override
    public long count() {
        return store.size();
    }
}
