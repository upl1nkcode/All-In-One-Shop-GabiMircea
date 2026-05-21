package com.allinoneshop.repository;

import com.allinoneshop.entity.Favorite;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryFavoriteRepository implements FavoriteRepository {

    private final Map<UUID, Favorite> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Favorite> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Favorite> findByUserId(UUID userId) {
        return store.values().stream()
                .filter(f -> f.getUser() != null && userId.equals(f.getUser().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Favorite> findByUserIdWithProducts(UUID userId) {
        return findByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndProductId(UUID userId, UUID productId) {
        return store.values().stream()
                .anyMatch(f -> f.getUser() != null && userId.equals(f.getUser().getId())
                        && f.getProduct() != null && productId.equals(f.getProduct().getId()));
    }

    @Override
    public void deleteByUserIdAndProductId(UUID userId, UUID productId) {
        store.values().removeIf(f -> f.getUser() != null && userId.equals(f.getUser().getId())
                && f.getProduct() != null && productId.equals(f.getProduct().getId()));
    }

    @Override
    public Favorite save(Favorite favorite) {
        if (favorite.getId() == null) favorite.setId(UUID.randomUUID());
        if (favorite.getCreatedAt() == null) favorite.setCreatedAt(OffsetDateTime.now());
        store.put(favorite.getId(), favorite);
        return favorite;
    }

    @Override
    public long count() {
        return store.size();
    }
}
