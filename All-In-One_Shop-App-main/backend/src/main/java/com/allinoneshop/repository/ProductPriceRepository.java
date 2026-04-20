package com.allinoneshop.repository;

import com.allinoneshop.entity.ProductPrice;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ProductPriceRepository {

    private final ConcurrentHashMap<UUID, ProductPrice> store = new ConcurrentHashMap<>();

    public ProductPrice save(ProductPrice price) {
        if (price.getId() == null) {
            price.setId(UUID.randomUUID());
            price.setCreatedAt(java.time.OffsetDateTime.now());
        }
        price.setLastChecked(java.time.OffsetDateTime.now());
        store.put(price.getId(), price);
        return price;
    }

    public Optional<ProductPrice> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<ProductPrice> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<ProductPrice> findByProductId(UUID productId) {
        return store.values().stream()
                .filter(pp -> pp.getProduct() != null && productId.equals(pp.getProduct().getId()))
                .collect(Collectors.toList());
    }

    public List<ProductPrice> findByStoreId(UUID storeId) {
        return store.values().stream()
                .filter(pp -> pp.getStore() != null && storeId.equals(pp.getStore().getId()))
                .collect(Collectors.toList());
    }

    public List<ProductPrice> findByProductIdOrderByPriceAsc(UUID productId) {
        return findByProductId(productId).stream()
                .sorted(Comparator.comparing(ProductPrice::getPrice))
                .collect(Collectors.toList());
    }

    public ProductPrice findByProductIdAndStoreId(UUID productId, UUID storeId) {
        return store.values().stream()
                .filter(pp -> pp.getProduct() != null && productId.equals(pp.getProduct().getId())
                        && pp.getStore() != null && storeId.equals(pp.getStore().getId()))
                .findFirst()
                .orElse(null);
    }

    public void deleteByStoreId(UUID storeId) {
        store.values().removeIf(pp -> pp.getStore() != null && storeId.equals(pp.getStore().getId()));
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
