package com.allinoneshop.repository;

import com.allinoneshop.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, UUID> {

    List<ProductPrice> findByProductId(UUID productId);

    List<ProductPrice> findByStoreId(UUID storeId);

    @Query("SELECT pp FROM ProductPrice pp " +
           "JOIN FETCH pp.store " +
           "WHERE pp.product.id = :productId " +
           "ORDER BY pp.price ASC")
    List<ProductPrice> findByProductIdOrderByPriceAsc(@Param("productId") UUID productId);

    @Query("SELECT pp FROM ProductPrice pp " +
           "JOIN FETCH pp.store " +
           "JOIN FETCH pp.product p " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE pp.store.id = :storeId")
    List<ProductPrice> findByStoreIdWithDetails(@Param("storeId") UUID storeId);

    @Query("SELECT pp FROM ProductPrice pp " +
           "WHERE pp.product.id = :productId AND pp.store.id = :storeId")
    ProductPrice findByProductIdAndStoreId(@Param("productId") UUID productId, @Param("storeId") UUID storeId);

    void deleteByStoreId(UUID storeId);
}
