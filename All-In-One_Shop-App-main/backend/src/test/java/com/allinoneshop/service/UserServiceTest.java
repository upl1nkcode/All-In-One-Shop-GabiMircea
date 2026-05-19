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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;

    private User createUser(String email, String firstName, String lastName) {
        return User.builder().id(UUID.randomUUID())
                .email(email).passwordHash("hash")
                .firstName(firstName).lastName(lastName)
                .role(Role.USER).build();
    }

    @Test
    void getUserProfile_found_returnsCorrectDTO() {
        User user = createUser("user@example.com", "John", "Doe");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserProfile(user.getId());
        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getRole()).isEqualTo("USER");
    }

    @Test
    void getUserProfile_notFound_throwsRuntimeException() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserProfile(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("User not found");
    }

    @Test
    void updateProfile_allFields_updatesAndSaves() {
        User user = createUser("u@example.com", "Old", "Name");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDTO result = userService.updateProfile(user.getId(), "New", "Surname", "https://img.com/a.png");
        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getLastName()).isEqualTo("Surname");
        assertThat(result.getAvatarUrl()).isEqualTo("https://img.com/a.png");
    }

    @Test
    void updateProfile_nullFields_doesNotOverwriteExistingValues() {
        User user = createUser("u2@example.com", "Original", "Name");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDTO result = userService.updateProfile(user.getId(), null, null, null);
        assertThat(result.getFirstName()).isEqualTo("Original");
        assertThat(result.getLastName()).isEqualTo("Name");
    }

    @Test
    void updateProfile_partialUpdate_onlyUpdatesProvidedFields() {
        User user = createUser("u3@example.com", "First", "Last");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDTO result = userService.updateProfile(user.getId(), "UpdatedFirst", null, null);
        assertThat(result.getFirstName()).isEqualTo("UpdatedFirst");
        assertThat(result.getLastName()).isEqualTo("Last");
    }

    @Test
    void updateProfile_notFound_throwsRuntimeException() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateProfile(UUID.randomUUID(), "First", "Last", null))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("User not found");
    }
}
