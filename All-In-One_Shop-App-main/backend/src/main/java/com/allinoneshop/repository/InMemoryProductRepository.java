package com.allinoneshop.repository;

import com.allinoneshop.entity.Product;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<UUID, Product> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Product> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Product findByIdWithDetails(UUID id) {
        return store.get(id);
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Product> findAllWithDetails() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Product> searchProducts(String query) {
        String lower = query.toLowerCase();
        return store.values().stream()
                .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(lower))
                        || (p.getDescription() != null && p.getDescription().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByCategorySlug(String slug) {
        return store.values().stream()
                .filter(p -> p.getCategory() != null && slug.equalsIgnoreCase(p.getCategory().getSlug()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByBrandName(String brandName) {
        return store.values().stream()
                .filter(p -> p.getBrand() != null && brandName.equalsIgnoreCase(p.getBrand().getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findSimilarProducts(UUID categoryId, UUID excludeId, int limit) {
        List<Product> results = store.values().stream()
                .filter(p -> p.getCategory() != null && categoryId.equals(p.getCategory().getId()))
                .filter(p -> !excludeId.equals(p.getId()))
                .collect(Collectors.toList());
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) product.setId(UUID.randomUUID());
        if (product.getCreatedAt() == null) product.setCreatedAt(OffsetDateTime.now());
        product.setUpdatedAt(OffsetDateTime.now());
        store.put(product.getId(), product);
        return product;
    }

    @Override
    public void delete(Product product) {
        store.remove(product.getId());
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
