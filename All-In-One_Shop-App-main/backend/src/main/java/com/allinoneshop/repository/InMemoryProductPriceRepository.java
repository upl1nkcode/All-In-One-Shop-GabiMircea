package com.allinoneshop.repository;

import com.allinoneshop.entity.ProductPrice;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryProductPriceRepository implements ProductPriceRepository {

    private final Map<UUID, ProductPrice> store = new ConcurrentHashMap<>();

    @Override
    public Optional<ProductPrice> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ProductPrice> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<ProductPrice> findByProductId(UUID productId) {
        return store.values().stream()
                .filter(pp -> pp.getProduct() != null && productId.equals(pp.getProduct().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductPrice> findByStoreId(UUID storeId) {
        return store.values().stream()
                .filter(pp -> pp.getStore() != null && storeId.equals(pp.getStore().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductPrice> findByProductIdOrderByPriceAsc(UUID productId) {
        return findByProductId(productId).stream()
                .sorted(Comparator.comparing(ProductPrice::getPrice))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductPrice> findByProductIdAndStoreId(UUID productId, UUID storeId) {
        return store.values().stream()
                .filter(pp -> pp.getProduct() != null && productId.equals(pp.getProduct().getId())
                        && pp.getStore() != null && storeId.equals(pp.getStore().getId()))
                .findFirst();
    }

    @Override
    public void deleteByStoreId(UUID storeId) {
        store.values().removeIf(pp -> pp.getStore() != null && storeId.equals(pp.getStore().getId()));
    }

    @Override
    public ProductPrice save(ProductPrice price) {
        if (price.getId() == null) price.setId(UUID.randomUUID());
        if (price.getCreatedAt() == null) {
            price.setCreatedAt(OffsetDateTime.now());
            price.setLastChecked(OffsetDateTime.now());
        }
        store.put(price.getId(), price);
        return price;
    }

    @Override
    public long count() {
        return store.size();
    }
}
