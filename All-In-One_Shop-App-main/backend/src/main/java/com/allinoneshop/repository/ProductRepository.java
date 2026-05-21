package com.allinoneshop.repository;

import com.allinoneshop.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Optional<Product> findById(UUID id);

    Product findByIdWithDetails(UUID id);

    List<Product> findAll();

    List<Product> findAllWithDetails();

    List<Product> searchProducts(String query);

    List<Product> findByCategorySlug(String slug);

    List<Product> findByBrandName(String brandName);

    List<Product> findSimilarProducts(UUID categoryId, UUID excludeId, int limit);

    Product save(Product product);

    void delete(Product product);

    void deleteById(UUID id);

    long count();
}
