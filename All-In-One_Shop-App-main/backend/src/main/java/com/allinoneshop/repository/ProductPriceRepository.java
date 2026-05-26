package com.allinoneshop.repository;

import com.allinoneshop.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, UUID> {
    List<ProductPrice> findByProductId(UUID productId);
    List<ProductPrice> findByStoreId(UUID storeId);
    List<ProductPrice> findByProductIdOrderByPriceAsc(UUID productId);
    Optional<ProductPrice> findByProductIdAndStoreId(UUID productId, UUID storeId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ProductPrice pp WHERE pp.store.id = :storeId")
    void deleteByStoreId(@Param("storeId") UUID storeId);
}
