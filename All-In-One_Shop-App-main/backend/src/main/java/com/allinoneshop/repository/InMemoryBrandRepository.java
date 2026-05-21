package com.allinoneshop.repository;

import com.allinoneshop.entity.Brand;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryBrandRepository implements BrandRepository {

    private final Map<UUID, Brand> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Brand> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Brand> findByName(String name) {
        return store.values().stream()
                .filter(b -> b.getName() != null && b.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Brand> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Brand save(Brand brand) {
        if (brand.getId() == null) brand.setId(UUID.randomUUID());
        if (brand.getCreatedAt() == null) brand.setCreatedAt(OffsetDateTime.now());
        store.put(brand.getId(), brand);
        return brand;
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
