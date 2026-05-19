package com.allinoneshop.repository;

import com.allinoneshop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlugIgnoreCase(String slug);

    Optional<Category> findByNameIgnoreCase(String name);

    default Optional<Category> findBySlug(String slug) {
        return findBySlugIgnoreCase(slug);
    }

    default Optional<Category> findByName(String name) {
        return findByNameIgnoreCase(name);
    }

    boolean existsByNameIgnoreCase(String name);
}
