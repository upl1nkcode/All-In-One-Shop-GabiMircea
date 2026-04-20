package com.allinoneshop.repository;

import com.allinoneshop.entity.Brand;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BrandRepository {

    private final ConcurrentHashMap<UUID, Brand> store = new ConcurrentHashMap<>();

    public Brand save(Brand brand) {
        if (brand.getId() == null) {
            brand.setId(UUID.randomUUID());
            brand.setCreatedAt(java.time.OffsetDateTime.now());
        }
        store.put(brand.getId(), brand);
        return brand;
    }

    public Optional<Brand> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Brand> findByName(String name) {
        return store.values().stream()
                .filter(b -> name.equalsIgnoreCase(b.getName()))
                .findFirst();
    }

    public List<Brand> findAll() {
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
