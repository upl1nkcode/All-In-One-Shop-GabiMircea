package com.allinoneshop.repository;

import com.allinoneshop.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(p.brand.name) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(p.category.name) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Product> searchProducts(@Param("q") String query);

    @Query("SELECT p FROM Product p WHERE p.category.slug = :slug")
    List<Product> findByCategorySlug(@Param("slug") String slug);

    List<Product> findByBrandName(String brandName);

    @Query("SELECT p FROM Product p WHERE p.category.id = :catId AND p.id <> :excludeId")
    List<Product> findSimilarProducts(@Param("catId") UUID categoryId,
                                      @Param("excludeId") UUID excludeId,
                                      Pageable pageable);

    default List<Product> findAllWithDetails() { return findAll(); }

    default Product findByIdWithDetails(UUID id) { return findById(id).orElse(null); }

    default List<Product> findSimilarProducts(UUID categoryId, UUID excludeId, int limit) {
        return findSimilarProducts(categoryId, excludeId, Pageable.ofSize(limit));
    }
}
