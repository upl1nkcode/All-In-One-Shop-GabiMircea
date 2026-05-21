package com.allinoneshop.repository;

import com.allinoneshop.entity.Category;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryCategoryRepository implements CategoryRepository {

    private final Map<UUID, Category> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Category> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return store.values().stream()
                .filter(c -> c.getSlug() != null && c.getSlug().equalsIgnoreCase(slug))
                .findFirst();
    }

    @Override
    public Optional<Category> findByName(String name) {
        return store.values().stream()
                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Category> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Category save(Category category) {
        if (category.getId() == null) category.setId(UUID.randomUUID());
        if (category.getCreatedAt() == null) category.setCreatedAt(OffsetDateTime.now());
        store.put(category.getId(), category);
        return category;
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
