package com.allinoneshop.repository;

import com.allinoneshop.entity.SearchHistory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class SearchHistoryRepository {

    private final ConcurrentHashMap<UUID, SearchHistory> store = new ConcurrentHashMap<>();

    public SearchHistory save(SearchHistory history) {
        if (history.getId() == null) {
            history.setId(UUID.randomUUID());
            history.setCreatedAt(java.time.OffsetDateTime.now());
        }
        store.put(history.getId(), history);
        return history;
    }

    public List<SearchHistory> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<SearchHistory> findByUserIdOrderByCreatedAtDesc(UUID userId, int limit) {
        return store.values().stream()
                .filter(sh -> sh.getUser() != null && userId.equals(sh.getUser().getId()))
                .sorted(Comparator.comparing(SearchHistory::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<String> findRecentSearchesByUserId(UUID userId, int limit) {
        return store.values().stream()
                .filter(sh -> sh.getUser() != null && userId.equals(sh.getUser().getId()))
                .sorted(Comparator.comparing(SearchHistory::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(SearchHistory::getSearchQuery)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<String> findTrendingSearches(int limit) {
        return store.values().stream()
                .collect(Collectors.groupingBy(SearchHistory::getSearchQuery, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void deleteByUserId(UUID userId) {
        store.values().removeIf(sh -> sh.getUser() != null && userId.equals(sh.getUser().getId()));
    }

    public void deleteById(UUID id) {
        store.remove(id);
    }

    public long count() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }
}
