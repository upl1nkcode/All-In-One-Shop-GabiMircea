package com.allinoneshop.repository;

import com.allinoneshop.entity.User;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {

    private final ConcurrentHashMap<UUID, User> store = new ConcurrentHashMap<>();

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
            user.setCreatedAt(java.time.OffsetDateTime.now());
        }
        user.setUpdatedAt(java.time.OffsetDateTime.now());
        store.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<User> findByEmail(String email) {
        return store.values().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst();
    }

    public boolean existsByEmail(String email) {
        return store.values().stream()
                .anyMatch(u -> email.equalsIgnoreCase(u.getEmail()));
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    public void deleteById(UUID id) {
        store.remove(id);
    }

    public long count() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }
}
