package com.allinoneshop.repository;

import com.allinoneshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.prices pp " +
           "LEFT JOIN FETCH pp.store " +
           "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(@Param("query") String query);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.prices pp " +
           "LEFT JOIN FETCH pp.store " +
           "WHERE p.category.slug = :categorySlug")
    List<Product> findByCategorySlug(@Param("categorySlug") String categorySlug);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.prices pp " +
           "LEFT JOIN FETCH pp.store " +
           "WHERE p.brand.name = :brandName")
    List<Product> findByBrandName(@Param("brandName") String brandName);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.prices pp " +
           "LEFT JOIN FETCH pp.store")
    List<Product> findAllWithDetails();

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.prices pp " +
           "LEFT JOIN FETCH pp.store " +
           "WHERE p.id = :id")
    Product findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT DISTINCT p FROM Product p " +
           "JOIN p.prices pp " +
           "WHERE pp.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                    @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.brand " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.prices pp " +
           "LEFT JOIN FETCH pp.store " +
           "WHERE p.category.id = :categoryId AND p.id != :productId")
    List<Product> findSimilarProducts(@Param("categoryId") UUID categoryId, 
                                       @Param("productId") UUID productId, 
                                       Pageable pageable);
}
