package com.allinoneshop.repository;

import com.allinoneshop.entity.SearchHistory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {

    @Query("SELECT sh.searchQuery FROM SearchHistory sh WHERE sh.user.id = :userId " +
           "ORDER BY sh.createdAt DESC")
    List<String> findRecentQueriesByUserId(@Param("userId") UUID userId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT sh.searchQuery FROM SearchHistory sh " +
           "GROUP BY sh.searchQuery ORDER BY COUNT(sh) DESC")
    List<String> findTopSearchQueries(org.springframework.data.domain.Pageable pageable);

    @Transactional
    @Modifying
    @Query("DELETE FROM SearchHistory sh WHERE sh.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    default List<String> findRecentSearchesByUserId(UUID userId, int limit) {
        return findRecentQueriesByUserId(userId, PageRequest.of(0, limit));
    }

    default List<String> findTrendingSearches(int limit) {
        return findTopSearchQueries(PageRequest.of(0, limit));
    }
}
