package com.allinoneshop.service;

import com.allinoneshop.dto.UserDTO;
import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import com.allinoneshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        userService = new UserService(userRepository);
    }

    private User createUser(String email, String firstName, String lastName) {
        return userRepository.save(User.builder()
                .email(email).passwordHash("hash")
                .firstName(firstName).lastName(lastName)
                .role(Role.USER).build());
    }

    // ── getUserProfile ────────────────────────────────────────

    @Test
    void getUserProfile_found_returnsCorrectDTO() {
        User user = createUser("user@example.com", "John", "Doe");

        UserDTO result = userService.getUserProfile(user.getId());

        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getRole()).isEqualTo("USER");
    }

    @Test
    void getUserProfile_notFound_throwsRuntimeException() {
        assertThatThrownBy(() -> userService.getUserProfile(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ── updateProfile ─────────────────────────────────────────

    @Test
    void updateProfile_allFields_updatesAndSaves() {
        User user = createUser("u@example.com", "Old", "Name");

        UserDTO result = userService.updateProfile(user.getId(), "New", "Surname", "https://img.com/a.png");

        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getLastName()).isEqualTo("Surname");
        assertThat(result.getAvatarUrl()).isEqualTo("https://img.com/a.png");
    }

    @Test
    void updateProfile_nullFields_doesNotOverwriteExistingValues() {
        User user = createUser("u2@example.com", "Original", "Name");

        UserDTO result = userService.updateProfile(user.getId(), null, null, null);

        assertThat(result.getFirstName()).isEqualTo("Original");
        assertThat(result.getLastName()).isEqualTo("Name");
    }

    @Test
    void updateProfile_partialUpdate_onlyUpdatesProvidedFields() {
        User user = createUser("u3@example.com", "First", "Last");

        UserDTO result = userService.updateProfile(user.getId(), "UpdatedFirst", null, null);

        assertThat(result.getFirstName()).isEqualTo("UpdatedFirst");
        assertThat(result.getLastName()).isEqualTo("Last");
    }

    @Test
    void updateProfile_notFound_throwsRuntimeException() {
        assertThatThrownBy(() -> userService.updateProfile(UUID.randomUUID(), "First", "Last", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateProfile_persistsToRepository() {
        User user = createUser("persist@example.com", "A", "B");

        userService.updateProfile(user.getId(), "X", "Y", null);

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("X");
        assertThat(updated.getLastName()).isEqualTo("Y");
    }
}
