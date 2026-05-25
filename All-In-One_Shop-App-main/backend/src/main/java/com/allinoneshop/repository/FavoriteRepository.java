package com.allinoneshop.repository;

import com.allinoneshop.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId")
    List<Favorite> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT f FROM Favorite f LEFT JOIN FETCH f.product WHERE f.user.id = :userId")
    List<Favorite> findByUserIdWithProducts(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Favorite f WHERE f.user.id = :userId AND f.product.id = :productId")
    boolean existsByUserIdAndProductId(@Param("userId") UUID userId, @Param("productId") UUID productId);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId AND f.product.id = :productId")
    void deleteByUserIdAndProductId(@Param("userId") UUID userId, @Param("productId") UUID productId);
}
