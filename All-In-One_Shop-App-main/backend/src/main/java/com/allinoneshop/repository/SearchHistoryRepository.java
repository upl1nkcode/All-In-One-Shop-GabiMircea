package com.allinoneshop.repository;

import com.allinoneshop.entity.SearchHistory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {

    @Query("SELECT s.searchQuery FROM SearchHistory s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<String> findRecentSearchesByUserIdInternal(@Param("userId") UUID userId, Pageable pageable);

    default List<String> findRecentSearchesByUserId(UUID userId, int limit) {
        return findRecentSearchesByUserIdInternal(userId, PageRequest.of(0, limit));
    }

    @Query("SELECT s.searchQuery FROM SearchHistory s GROUP BY s.searchQuery ORDER BY COUNT(s) DESC")
    List<String> findTrendingSearchesInternal(Pageable pageable);

    default List<String> findTrendingSearches(int limit) {
        return findTrendingSearchesInternal(PageRequest.of(0, limit));
    }

    @Modifying
    @Query("DELETE FROM SearchHistory s WHERE s.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
