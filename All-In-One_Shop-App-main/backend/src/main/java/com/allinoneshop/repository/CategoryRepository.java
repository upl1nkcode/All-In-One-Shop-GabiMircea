package com.allinoneshop.repository;

import com.allinoneshop.entity.Category;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CategoryRepository {

    private final ConcurrentHashMap<UUID, Category> store = new ConcurrentHashMap<>();

    public Category save(Category category) {
        if (category.getId() == null) {
            category.setId(UUID.randomUUID());
            category.setCreatedAt(java.time.OffsetDateTime.now());
        }
        store.put(category.getId(), category);
        return category;
    }

    public Optional<Category> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Category> findBySlug(String slug) {
        return store.values().stream()
                .filter(c -> slug.equalsIgnoreCase(c.getSlug()))
                .findFirst();
    }

    public Optional<Category> findByName(String name) {
        return store.values().stream()
                .filter(c -> name.equalsIgnoreCase(c.getName()))
                .findFirst();
    }

    public List<Category> findAll() {
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
