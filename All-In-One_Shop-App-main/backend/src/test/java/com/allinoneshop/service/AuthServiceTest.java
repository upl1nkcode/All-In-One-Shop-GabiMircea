package com.allinoneshop.service;

import com.allinoneshop.dto.UserDTO;
import com.allinoneshop.dto.auth.*;
import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import com.allinoneshop.repository.UserRepository;
import com.allinoneshop.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthenticationManager authenticationManager;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider, authenticationManager);
    }

    // ── register ──────────────────────────────────────────────

    @Test
    void register_newEmail_savesUserAndReturnsToken() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwt.token");

        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@example.com");
        req.setPassword("secure123");
        req.setFirstName("Jane");
        req.setLastName("Doe");

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("new@example.com");
        assertThat(response.getUser().getFirstName()).isEqualTo("Jane");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_existingEmail_throwsAndNeverSaves() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        RegisterRequest dup = new RegisterRequest();
        dup.setEmail("taken@example.com");
        dup.setPassword("pass5678");

        assertThatThrownBy(() -> authService.register(dup))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_passwordIsHashed() {
        when(userRepository.existsByEmail("hash@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("t");

        RegisterRequest req = new RegisterRequest();
        req.setEmail("hash@example.com");
        req.setPassword("plaintext");
        authService.register(req);

        verify(userRepository).save(argThat(user -> {
            assertThat(user.getPasswordHash()).isNotEqualTo("plaintext");
            assertThat(passwordEncoder.matches("plaintext", user.getPasswordHash())).isTrue();
            return true;
        }));
    }

    @Test
    void register_setsRoleUser() {
        when(userRepository.existsByEmail("role@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("t");

        RegisterRequest req = new RegisterRequest();
        req.setEmail("role@example.com");
        req.setPassword("pass1234");
        AuthResponse response = authService.register(req);

        assertThat(response.getUser().getRole()).isEqualTo("USER");
    }

    // ── login ─────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsTokenAndUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN).firstName("Admin").lastName("User")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtTokenProvider.generateToken(user)).thenReturn("valid.jwt");

        LoginRequest req = new LoginRequest();
        req.setEmail("admin@example.com");
        req.setPassword("admin123");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("valid.jwt");
        assertThat(response.getUser().getEmail()).isEqualTo("admin@example.com");
        assertThat(response.getUser().getRole()).isEqualTo("ADMIN");
    }

    @Test
    void login_badCredentials_propagatesException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_callsAuthManagerWithCorrectCredentials() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("u@test.com").passwordHash("h").role(Role.USER).build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(any())).thenReturn("t");

        LoginRequest req = new LoginRequest();
        req.setEmail("u@test.com");
        req.setPassword("mypass");
        authService.login(req);

        verify(authenticationManager).authenticate(
                argThat(a -> {
                    UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) a;
                    return "u@test.com".equals(token.getPrincipal())
                            && "mypass".equals(token.getCredentials());
                })
        );
    }

    // ── getCurrentUser ────────────────────────────────────────

    @Test
    void getCurrentUser_existingEmail_returnsDTO() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("admin@allinone.com").passwordHash("h")
                .firstName("Admin").lastName("User").role(Role.ADMIN)
                .build();
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
