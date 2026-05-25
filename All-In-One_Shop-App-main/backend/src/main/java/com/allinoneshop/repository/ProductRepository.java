package com.allinoneshop.repository;

import com.allinoneshop.entity.Product;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.prices WHERE p.id = :id")
    Product findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.prices")
    List<Product> findAllWithDetails();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE p.category.slug = :slug")
    List<Product> findByCategorySlug(@Param("slug") String slug);

    @Query("SELECT p FROM Product p WHERE LOWER(p.brand.name) = LOWER(:brandName)")
    List<Product> findByBrandName(@Param("brandName") String brandName);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id != :excludeId")
    List<Product> findSimilarProductsInternal(@Param("categoryId") UUID categoryId, @Param("excludeId") UUID excludeId, Pageable pageable);

    default List<Product> findSimilarProducts(UUID categoryId, UUID excludeId, int limit) {
        return findSimilarProductsInternal(categoryId, excludeId, PageRequest.of(0, limit));
    }

    void delete(Product product);
}
