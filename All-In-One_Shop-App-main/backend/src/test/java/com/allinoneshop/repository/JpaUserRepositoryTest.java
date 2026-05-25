package com.allinoneshop.repository;

import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class JpaUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String email) {
        return User.builder()
                .email(email)
                .passwordHash("$2a$10$hash")
                .firstName("John")
                .lastName("Doe")
                .role(Role.USER)
                .build();
    }

    @Test
    void save_persistsUser() {
        User saved = userRepository.save(buildUser("alice@test.com"));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findById_existing_returnsUser() {
        User saved = userRepository.save(buildUser("bob@test.com"));
        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("bob@test.com");
    }

    @Test
    void findById_nonExisting_returnsEmpty() {
        assertThat(userRepository.findById(java.util.UUID.randomUUID())).isEmpty();
    }

    @Test
    void findByEmail_existing_returnsUser() {
        userRepository.save(buildUser("carol@test.com"));
        Optional<User> found = userRepository.findByEmail("carol@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void findByEmail_nonExisting_returnsEmpty() {
        assertThat(userRepository.findByEmail("nobody@test.com")).isEmpty();
    }

    @Test
    void existsByEmail_existing_returnsTrue() {
        userRepository.save(buildUser("dave@test.com"));
        assertThat(userRepository.existsByEmail("dave@test.com")).isTrue();
    }

    @Test
    void existsByEmail_nonExisting_returnsFalse() {
        assertThat(userRepository.existsByEmail("ghost@test.com")).isFalse();
    }

    @Test
    void findAll_returnsAllSavedUsers() {
        userRepository.save(buildUser("u1@test.com"));
        userRepository.save(buildUser("u2@test.com"));
        assertThat(userRepository.findAll()).hasSize(2);
    }

    @Test
    void deleteById_removesUser() {
        User saved = userRepository.save(buildUser("del@test.com"));
        userRepository.deleteById(saved.getId());
        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void count_reflectsSavedCount() {
        assertThat(userRepository.count()).isZero();
        userRepository.save(buildUser("cnt1@test.com"));
        userRepository.save(buildUser("cnt2@test.com"));
        assertThat(userRepository.count()).isEqualTo(2);
    }

    @Test
    void save_updatesExistingUser() {
        User saved = userRepository.save(buildUser("update@test.com"));
        saved.setFirstName("Jane");
        userRepository.save(saved);
        User updated = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("Jane");
    }

    @Test
    void uniqueEmail_enforced() {
        userRepository.save(buildUser("unique@test.com"));
        assertThatThrownBy(() -> {
            userRepository.save(buildUser("unique@test.com"));
            userRepository.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    void defaultRole_isUser() {
        User saved = userRepository.save(buildUser("role@test.com"));
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }
}
