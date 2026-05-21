package com.allinoneshop.controller;

import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration security tests with the full Spring context.
 * Tests URL-level security: 401 for unauthenticated, 403 for insufficient role.
 */
@SpringBootTest(properties = {"server.ssl.enabled=false", "server.servlet.context-path="})
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired private MockMvc mockMvc;

    private User regularUser() {
        return User.builder().id(UUID.randomUUID()).email("sec-user@test.com")
                .passwordHash("h").role(Role.USER).build();
    }

    private User adminUser() {
        return User.builder().id(UUID.randomUUID()).email("sec-admin@test.com")
                .passwordHash("h").role(Role.ADMIN).build();
    }

    // ── Anonymous access to authenticated endpoints returns 401 ───────────────

    @Test
    void favorites_anonymous_returns401() throws Exception {
        mockMvc.perform(get("/favorites"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addFavorite_anonymous_returns401() throws Exception {
        mockMvc.perform(post("/favorites/{id}", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userProfile_anonymous_returns401() throws Exception {
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    // ── URL-level admin restriction: /admin/stats requires ADMIN ──────────────

    @Test
    void adminStats_asUser_returns403() throws Exception {
        mockMvc.perform(get("/admin/stats")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                regularUser(), null, regularUser().getAuthorities()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminStats_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/admin/stats")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                adminUser(), null, adminUser().getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── Authenticated access to protected endpoints succeeds ──────────────────

    @Test
    void favorites_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/favorites")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                regularUser(), null, regularUser().getAuthorities()))))
                .andExpect(status().isOk());
    }

    // ── Auth/me: null user → controller returns 401 body ─────────────────────

    @Test
    void authMe_anonymous_returnsUnauthenticated() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
