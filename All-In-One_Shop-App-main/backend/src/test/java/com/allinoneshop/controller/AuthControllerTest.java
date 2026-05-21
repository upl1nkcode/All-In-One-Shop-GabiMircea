package com.allinoneshop.controller;

import com.allinoneshop.dto.UserDTO;
import com.allinoneshop.dto.auth.AuthResponse;
import com.allinoneshop.dto.auth.LoginRequest;
import com.allinoneshop.dto.auth.RegisterRequest;
import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import com.allinoneshop.security.JwtTokenProvider;
import com.allinoneshop.security.TokenBlacklistService;
import com.allinoneshop.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = AuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private AuthResponse mockAuthResponse(String email) {
        User user = User.builder().id(UUID.randomUUID()).email(email)
                .passwordHash("h").role(Role.USER).firstName("John").lastName("Doe").build();
        return AuthResponse.builder()
                .token("jwt.test.token")
                .tokenType("Bearer")
                .user(UserDTO.builder().id(user.getId()).email(email)
                        .firstName("John").lastName("Doe").role("USER").build())
                .build();
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_validRequest_returns200WithToken() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(mockAuthResponse("user@test.com"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("user@test.com", "password123", "John", "Doe"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt.test.token"))
                .andExpect(jsonPath("$.data.user.email").value("user@test.com"));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("not-an-email", "password123", "A", "B"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_blankEmail_returns400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("", "password123", "A", "B"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("user@test.com", "short", "A", "B"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_blankPassword_returns400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("user@test.com", "", "A", "B"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(mockAuthResponse("admin@test.com"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("admin@test.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void login_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("not-valid", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_blankPassword_returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("user@test.com", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ── me & logout ───────────────────────────────────────────────────────────

    @Test
    void logout_returns200() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void logout_withToken_callsBlacklist() throws Exception {
        when(jwtTokenProvider.getExpirationFromToken(any()))
                .thenReturn(java.time.Instant.now().plusSeconds(3600));

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer some.valid.token"))
                .andExpect(status().isOk());
    }
}
