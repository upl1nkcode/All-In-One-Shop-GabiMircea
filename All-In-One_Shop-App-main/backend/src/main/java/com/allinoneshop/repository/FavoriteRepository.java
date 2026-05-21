package com.allinoneshop.repository;

import com.allinoneshop.entity.Favorite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository {

    Optional<Favorite> findById(UUID id);

    List<Favorite> findByUserId(UUID userId);

    List<Favorite> findByUserIdWithProducts(UUID userId);

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    void deleteByUserIdAndProductId(UUID userId, UUID productId);

    Favorite save(Favorite favorite);

    long count();
}
