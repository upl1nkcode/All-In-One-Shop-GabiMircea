package com.allinoneshop.repository;

import com.allinoneshop.entity.Favorite;
import com.allinoneshop.entity.Product;
import com.allinoneshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    
    @Query("SELECT f FROM Favorite f " +
           "JOIN FETCH f.product p " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.prices pp " +
           "LEFT JOIN FETCH pp.store " +
           "WHERE f.user.id = :userId")
    List<Favorite> findByUserIdWithProducts(@Param("userId") UUID userId);

    List<Favorite> findByUserId(UUID userId);

    Optional<Favorite> findByUserAndProduct(User user, Product product);

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    void deleteByUserIdAndProductId(UUID userId, UUID productId);
}
