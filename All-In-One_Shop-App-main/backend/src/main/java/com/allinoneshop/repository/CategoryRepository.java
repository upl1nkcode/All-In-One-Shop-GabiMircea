package com.allinoneshop.repository;

import com.allinoneshop.entity.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Optional<Category> findById(UUID id);

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);

    List<Category> findAll();

    Category save(Category category);

    void deleteById(UUID id);

    long count();
}
