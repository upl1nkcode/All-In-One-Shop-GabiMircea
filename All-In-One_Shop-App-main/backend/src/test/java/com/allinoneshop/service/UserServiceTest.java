package com.allinoneshop.service;

import com.allinoneshop.dto.UserDTO;
import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import com.allinoneshop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // ── helpers ───────────────────────────────────────────────

    private User buildUser(UUID id, String firstName, String lastName) {
        User user = new User();
        user.setId(id);
        user.setEmail("user@example.com");
        user.setPasswordHash("hash");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.USER);
        return user;
    }

    // ── getUserProfile ────────────────────────────────────────

    @Test
    void getUserProfile_found_returnsCorrectDTO() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "John", "Doe");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserProfile(userId);

        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getRole()).isEqualTo("USER");
    }

    @Test
    void getUserProfile_notFound_throwsRuntimeException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // ── updateProfile ─────────────────────────────────────────

    @Test
    void updateProfile_allFields_updatesAndSaves() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "Old", "Name");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDTO result = userService.updateProfile(userId, "New", "Surname", "https://img.com/a.png");

        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getLastName()).isEqualTo("Surname");
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_nullFields_doesNotOverwriteExistingValues() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "Original", "Name");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDTO result = userService.updateProfile(userId, null, null, null);

        assertThat(result.getFirstName()).isEqualTo("Original");
        assertThat(result.getLastName()).isEqualTo("Name");
    }

    @Test
    void updateProfile_partialUpdate_onlyUpdatesProvidedFields() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "First", "Last");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDTO result = userService.updateProfile(userId, "UpdatedFirst", null, null);

        assertThat(result.getFirstName()).isEqualTo("UpdatedFirst");
        assertThat(result.getLastName()).isEqualTo("Last");
    }

    @Test
    void updateProfile_notFound_throwsRuntimeException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(userId, "First", "Last", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateProfile_savesOnce() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "A", "B");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateProfile(userId, "X", "Y", null);

        verify(userRepository, times(1)).save(user);
    }
}
