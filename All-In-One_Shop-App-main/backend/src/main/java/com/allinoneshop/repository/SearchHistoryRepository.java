package com.allinoneshop.repository;

import com.allinoneshop.entity.SearchHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {

    List<SearchHistory> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT sh.searchQuery, COUNT(sh) as count FROM SearchHistory sh " +
           "GROUP BY sh.searchQuery ORDER BY count DESC")
    List<Object[]> findTrendingSearches(Pageable pageable);

    @Query("SELECT DISTINCT sh.searchQuery FROM SearchHistory sh " +
           "WHERE sh.user.id = :userId " +
           "ORDER BY sh.createdAt DESC")
    List<String> findRecentSearchesByUserId(@Param("userId") UUID userId, Pageable pageable);
}
