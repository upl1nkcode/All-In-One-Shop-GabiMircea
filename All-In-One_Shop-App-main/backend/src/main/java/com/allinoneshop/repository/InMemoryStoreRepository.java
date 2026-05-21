package com.allinoneshop.repository;

import com.allinoneshop.entity.Store;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryStoreRepository implements StoreRepository {

    private final Map<UUID, Store> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Store> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Store> findByName(String name) {
        return store.values().stream()
                .filter(s -> s.getName() != null && s.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Store> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Store save(Store s) {
        if (s.getId() == null) s.setId(UUID.randomUUID());
        if (s.getCreatedAt() == null) s.setCreatedAt(OffsetDateTime.now());
        s.setUpdatedAt(OffsetDateTime.now());
        store.put(s.getId(), s);
        return s;
    }

    @Override
    public void deleteById(UUID id) {
        store.remove(id);
    }

    @Override
    public long count() {
        return store.size();
    }
}
