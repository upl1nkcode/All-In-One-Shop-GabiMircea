package com.allinoneshop.repository;

import com.allinoneshop.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, UUID> {

    @Query("SELECT pp FROM ProductPrice pp WHERE pp.product.id = :productId")
    List<ProductPrice> findByProductId(@Param("productId") UUID productId);

    @Query("SELECT pp FROM ProductPrice pp WHERE pp.store.id = :storeId")
    List<ProductPrice> findByStoreId(@Param("storeId") UUID storeId);

    @Query("SELECT pp FROM ProductPrice pp WHERE pp.product.id = :productId ORDER BY pp.price ASC")
    List<ProductPrice> findByProductIdOrderByPriceAsc(@Param("productId") UUID productId);

    @Query("SELECT pp FROM ProductPrice pp WHERE pp.product.id = :productId AND pp.store.id = :storeId")
    Optional<ProductPrice> findByProductIdAndStoreId(@Param("productId") UUID productId, @Param("storeId") UUID storeId);

    @Modifying
    @Query("DELETE FROM ProductPrice pp WHERE pp.store.id = :storeId")
    void deleteByStoreId(@Param("storeId") UUID storeId);
}
