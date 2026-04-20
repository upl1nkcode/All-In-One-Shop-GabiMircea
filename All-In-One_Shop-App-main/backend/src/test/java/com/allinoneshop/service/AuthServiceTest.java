package com.allinoneshop.service;

import com.allinoneshop.dto.UserDTO;
import com.allinoneshop.dto.auth.AuthResponse;
import com.allinoneshop.dto.auth.LoginRequest;
import com.allinoneshop.dto.auth.RegisterRequest;
import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import com.allinoneshop.repository.UserRepository;
import com.allinoneshop.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // ── helpers ───────────────────────────────────────────────

    private User buildUser(String email, Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("$2b$hashed");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(role);
        return user;
    }

    // ── register ──────────────────────────────────────────────

    @Test
    void register_newEmail_savesUserAndReturnsToken() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@example.com");
        req.setPassword("secure123");
        req.setFirstName("Jane");
        req.setLastName("Doe");

        User saved = buildUser("new@example.com", Role.ADMIN);

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secure123")).thenReturn("$2b$hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtTokenProvider.generateToken(saved)).thenReturn("jwt.token.here");

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("secure123");
    }

    @Test
    void register_existingEmail_throwsAndNeverSaves() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("taken@example.com");
        req.setPassword("pass");

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_passwordIsEncoded() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("user@example.com");
        req.setPassword("plaintext");

        User saved = buildUser("user@example.com", Role.ADMIN);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("$2b$encoded");
        when(userRepository.save(any())).thenReturn(saved);
        when(jwtTokenProvider.generateToken(any())).thenReturn("token");

        authService.register(req);

        verify(passwordEncoder).encode("plaintext");
        verify(userRepository).save(argThat(u -> u.getPasswordHash().equals("$2b$encoded")));
    }

    // ── login ─────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsTokenAndUser() {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@example.com");
        req.setPassword("admin123");

        User user = buildUser("admin@example.com", Role.ADMIN);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtTokenProvider.generateToken(user)).thenReturn("valid.jwt");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("valid.jwt");
        assertThat(response.getUser().getEmail()).isEqualTo("admin@example.com");
        assertThat(response.getUser().getRole()).isEqualTo("ADMIN");
    }

    @Test
    void login_badCredentials_propagatesException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_callsAuthManagerWithCorrectCredentials() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("mypass");

        User user = buildUser("user@test.com", Role.ADMIN);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(any())).thenReturn("t");

        authService.login(req);

        verify(authenticationManager).authenticate(
                argThat(a -> {
                    UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) a;
                    return "user@test.com".equals(token.getPrincipal())
                            && "mypass".equals(token.getCredentials());
                })
        );
    }

    // ── getCurrentUser ────────────────────────────────────────

    @Test
    void getCurrentUser_existingEmail_returnsDTO() {
        User user = buildUser("admin@allinone.com", Role.ADMIN);
        when(userRepository.findByEmail("admin@allinone.com")).thenReturn(Optional.of(user));

        UserDTO result = authService.getCurrentUser("admin@allinone.com");

        assertThat(result.getEmail()).isEqualTo("admin@allinone.com");
        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void getCurrentUser_unknownEmail_throwsRuntimeException() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser("ghost@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }
}
