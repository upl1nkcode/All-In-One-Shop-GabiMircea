package com.allinoneshop.repository;

import com.allinoneshop.entity.Favorite;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class FavoriteRepository {

    private final ConcurrentHashMap<UUID, Favorite> store = new ConcurrentHashMap<>();

    public Favorite save(Favorite favorite) {
        if (favorite.getId() == null) {
            favorite.setId(UUID.randomUUID());
            favorite.setCreatedAt(java.time.OffsetDateTime.now());
        }
        store.put(favorite.getId(), favorite);
        return favorite;
    }

    public Optional<Favorite> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Favorite> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<Favorite> findByUserId(UUID userId) {
        return store.values().stream()
                .filter(f -> f.getUser() != null && userId.equals(f.getUser().getId()))
                .collect(Collectors.toList());
    }

    public List<Favorite> findByUserIdWithProducts(UUID userId) {
        return findByUserId(userId);
    }

    public boolean existsByUserIdAndProductId(UUID userId, UUID productId) {
        return store.values().stream()
                .anyMatch(f -> f.getUser() != null && userId.equals(f.getUser().getId())
                        && f.getProduct() != null && productId.equals(f.getProduct().getId()));
    }

    public void deleteByUserIdAndProductId(UUID userId, UUID productId) {
        store.values().removeIf(f ->
                f.getUser() != null && userId.equals(f.getUser().getId())
                        && f.getProduct() != null && productId.equals(f.getProduct().getId()));
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
