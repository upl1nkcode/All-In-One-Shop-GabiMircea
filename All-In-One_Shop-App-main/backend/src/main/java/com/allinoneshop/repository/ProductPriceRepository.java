package com.allinoneshop.repository;

import com.allinoneshop.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, UUID> {

    List<ProductPrice> findByProductId(UUID productId);

    List<ProductPrice> findByStoreId(UUID storeId);

    List<ProductPrice> findByProductIdOrderByPriceAsc(UUID productId);

    Optional<ProductPrice> findByProductIdAndStoreId(UUID productId, UUID storeId);

    void deleteByStoreId(UUID storeId);
}
