package com.allinoneshop.repository;

import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryUserRepository();
    }

    private User buildUser(String email) {
        return User.builder()
                .email(email)
                .passwordHash("hash")
                .role(Role.USER)
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void save_generatesId() {
        User user = buildUser("a@test.com");
        User saved = repo.save(user);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void save_setsTimestamps() {
        User saved = repo.save(buildUser("b@test.com"));
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void save_updatePreservesId() {
        User user = repo.save(buildUser("c@test.com"));
        user.setFirstName("Updated");
        repo.save(user);
        assertThat(repo.count()).isEqualTo(1);
        assertThat(repo.findById(user.getId()).get().getFirstName()).isEqualTo("Updated");
    }

    @Test
    void findById_existing_returnsUser() {
        User user = repo.save(buildUser("d@test.com"));
        Optional<User> found = repo.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("d@test.com");
    }

    @Test
    void findById_nonExisting_returnsEmpty() {
        assertThat(repo.findById(java.util.UUID.randomUUID())).isEmpty();
    }

    @Test
    void findByEmail_caseInsensitive_returnsUser() {
        repo.save(buildUser("CaseSensitive@Test.COM"));
        assertThat(repo.findByEmail("casesensitive@test.com")).isPresent();
        assertThat(repo.findByEmail("CASESENSITIVE@TEST.COM")).isPresent();
    }

    @Test
    void findByEmail_nonExisting_returnsEmpty() {
        assertThat(repo.findByEmail("nobody@nowhere.com")).isEmpty();
    }

    @Test
    void existsByEmail_existing_returnsTrue() {
        repo.save(buildUser("exists@test.com"));
        assertThat(repo.existsByEmail("exists@test.com")).isTrue();
    }

    @Test
    void existsByEmail_nonExisting_returnsFalse() {
        assertThat(repo.existsByEmail("ghost@test.com")).isFalse();
    }

    @Test
    void findAll_returnsAllUsers() {
        repo.save(buildUser("u1@test.com"));
        repo.save(buildUser("u2@test.com"));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void deleteById_removesUser() {
        User user = repo.save(buildUser("del@test.com"));
        repo.deleteById(user.getId());
        assertThat(repo.findById(user.getId())).isEmpty();
        assertThat(repo.count()).isZero();
    }

    @Test
    void count_reflectsStoredUsers() {
        assertThat(repo.count()).isZero();
        repo.save(buildUser("x@test.com"));
        repo.save(buildUser("y@test.com"));
        assertThat(repo.count()).isEqualTo(2);
    }
}
