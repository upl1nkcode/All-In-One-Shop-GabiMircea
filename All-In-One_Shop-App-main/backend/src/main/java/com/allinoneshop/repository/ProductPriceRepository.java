package com.allinoneshop.repository;

import com.allinoneshop.entity.ProductPrice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductPriceRepository {

    Optional<ProductPrice> findById(UUID id);

    List<ProductPrice> findAll();

    List<ProductPrice> findByProductId(UUID productId);

    List<ProductPrice> findByStoreId(UUID storeId);

    List<ProductPrice> findByProductIdOrderByPriceAsc(UUID productId);

    Optional<ProductPrice> findByProductIdAndStoreId(UUID productId, UUID storeId);

    void deleteByStoreId(UUID storeId);

    ProductPrice save(ProductPrice price);

    long count();
}
