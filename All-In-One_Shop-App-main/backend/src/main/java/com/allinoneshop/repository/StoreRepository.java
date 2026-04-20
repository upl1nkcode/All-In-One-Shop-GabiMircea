package com.allinoneshop.repository;

import com.allinoneshop.entity.Store;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class StoreRepository {

    private final ConcurrentHashMap<UUID, Store> store = new ConcurrentHashMap<>();

    public Store save(Store entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
            entity.setCreatedAt(java.time.OffsetDateTime.now());
        }
        entity.setUpdatedAt(java.time.OffsetDateTime.now());
        store.put(entity.getId(), entity);
        return entity;
    }

    public Optional<Store> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Store> findByName(String name) {
        return store.values().stream()
                .filter(s -> name.equalsIgnoreCase(s.getName()))
                .findFirst();
    }

    public List<Store> findAll() {
        return new ArrayList<>(store.values());
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
