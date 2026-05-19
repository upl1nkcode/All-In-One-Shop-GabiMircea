package com.allinoneshop.repository;

import com.allinoneshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category")
    List<Product> findAllWithDetails();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.prices pp LEFT JOIN FETCH pp.store WHERE p.id = :id")
    Product findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category " +
           "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(@Param("query") String query);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category " +
           "WHERE p.category.slug = :slug")
    List<Product> findByCategorySlug(@Param("slug") String slug);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category " +
           "WHERE LOWER(p.brand.name) = LOWER(:brandName)")
    List<Product> findByBrandName(@Param("brandName") String brandName);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category " +
           "WHERE p.category.id = :categoryId AND p.id <> :excludeId")
    List<Product> findByCategoryIdAndIdNot(@Param("categoryId") UUID categoryId,
                                           @Param("excludeId") UUID excludeId);

    default List<Product> findSimilarProducts(UUID categoryId, UUID excludeProductId, int limit) {
        List<Product> results = findByCategoryIdAndIdNot(categoryId, excludeProductId);
        return results.size() > limit ? results.subList(0, limit) : results;
    }
}
