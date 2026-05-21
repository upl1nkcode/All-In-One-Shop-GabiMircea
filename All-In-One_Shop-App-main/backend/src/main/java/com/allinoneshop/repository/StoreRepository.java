package com.allinoneshop.repository;

import com.allinoneshop.entity.Store;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository {

    Optional<Store> findById(UUID id);

    Optional<Store> findByName(String name);

    List<Store> findAll();

    Store save(Store store);

    void deleteById(UUID id);

    long count();
}
