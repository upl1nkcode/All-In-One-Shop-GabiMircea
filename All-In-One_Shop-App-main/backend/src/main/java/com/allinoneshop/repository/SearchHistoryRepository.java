package com.allinoneshop.repository;

import com.allinoneshop.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {

    List<SearchHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT sh.searchQuery FROM SearchHistory sh WHERE sh.user.id = :userId " +
           "GROUP BY sh.searchQuery ORDER BY MAX(sh.createdAt) DESC")
    List<String> findRecentSearchesByUserId(@Param("userId") UUID userId);

    default List<String> findRecentSearchesByUserId(UUID userId, int limit) {
        List<String> results = findRecentSearchesByUserId(userId);
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    @Query("SELECT sh.searchQuery FROM SearchHistory sh " +
           "GROUP BY sh.searchQuery ORDER BY COUNT(sh) DESC")
    List<String> findTrendingSearchesAll();

    default List<String> findTrendingSearches(int limit) {
        List<String> results = findTrendingSearchesAll();
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    void deleteByUserId(UUID userId);
}
