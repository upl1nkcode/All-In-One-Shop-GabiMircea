package com.allinoneshop.repository;

import com.allinoneshop.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);

    List<User> findAll();

    void deleteById(UUID id);

    long count();
}
