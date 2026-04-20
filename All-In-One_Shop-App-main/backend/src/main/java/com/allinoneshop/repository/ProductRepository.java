package com.allinoneshop.repository;

import com.allinoneshop.entity.Product;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {

    private final ConcurrentHashMap<UUID, Product> store = new ConcurrentHashMap<>();

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
            product.setCreatedAt(java.time.OffsetDateTime.now());
        }
        product.setUpdatedAt(java.time.OffsetDateTime.now());
        store.put(product.getId(), product);
        return product;
    }

    public Optional<Product> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    public void delete(Product product) {
        store.remove(product.getId());
    }

    public void deleteById(UUID id) {
        store.remove(id);
    }

    public long count() {
        return store.size();
    }

    public List<Product> findAllWithDetails() {
        return new ArrayList<>(store.values());
    }

    public Product findByIdWithDetails(UUID id) {
        return store.get(id);
    }

    public List<Product> searchProducts(String query) {
        String lowerQuery = query.toLowerCase();
        return store.values().stream()
                .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(lowerQuery))
                        || (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
    }

    public List<Product> findByCategorySlug(String categorySlug) {
        return store.values().stream()
                .filter(p -> p.getCategory() != null && categorySlug.equals(p.getCategory().getSlug()))
                .collect(Collectors.toList());
    }

    public List<Product> findByBrandName(String brandName) {
        return store.values().stream()
                .filter(p -> p.getBrand() != null && brandName.equalsIgnoreCase(p.getBrand().getName()))
                .collect(Collectors.toList());
    }

    public List<Product> findSimilarProducts(UUID categoryId, UUID excludeProductId, int limit) {
        return store.values().stream()
                .filter(p -> p.getCategory() != null
                        && p.getCategory().getId().equals(categoryId)
                        && !p.getId().equals(excludeProductId))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void clear() {
        store.clear();
    }
}
