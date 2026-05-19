package com.allinoneshop.repository;

import com.allinoneshop.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Optional<Brand> findByNameIgnoreCase(String name);

    default Optional<Brand> findByName(String name) {
        return findByNameIgnoreCase(name);
    }

    boolean existsByNameIgnoreCase(String name);
}
