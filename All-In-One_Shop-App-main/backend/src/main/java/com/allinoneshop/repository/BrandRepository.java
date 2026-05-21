package com.allinoneshop.repository;

import com.allinoneshop.entity.Brand;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandRepository {

    Optional<Brand> findById(UUID id);

    Optional<Brand> findByName(String name);

    List<Brand> findAll();

    Brand save(Brand brand);

    void deleteById(UUID id);

    long count();
}
