package com.allinoneshop.repository;

import com.allinoneshop.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByNameIgnoreCase(String name);

    default Optional<Store> findByName(String name) {
        return findByNameIgnoreCase(name);
    }

    boolean existsByNameIgnoreCase(String name);
}
