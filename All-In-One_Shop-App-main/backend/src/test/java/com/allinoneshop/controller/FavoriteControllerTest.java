package com.allinoneshop.controller;

import com.allinoneshop.dto.ProductDTO;
import com.allinoneshop.entity.User;
import com.allinoneshop.entity.enums.Role;
import com.allinoneshop.service.FavoriteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoriteController.class)
class FavoriteControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private FavoriteService favoriteService;
    @MockBean private com.allinoneshop.security.JwtTokenProvider jwtTokenProvider;
    @MockBean private com.allinoneshop.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private User testUser() {
        return User.builder().id(UUID.randomUUID()).email("user@test.com")
                .passwordHash("h").role(Role.USER).build();
    }

    private UsernamePasswordAuthenticationToken authToken(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void getFavorites_authenticated_returns200WithList() throws Exception {
        User user = testUser();
        ProductDTO product = ProductDTO.builder().id(UUID.randomUUID()).name("Nike Shoe").build();
        when(favoriteService.getUserFavorites(user.getId())).thenReturn(List.of(product));

        mockMvc.perform(get("/favorites").with(authentication(authToken(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Nike Shoe"));
    }

    @Test
    void getFavorites_emptyList_returns200() throws Exception {
        User user = testUser();
        when(favoriteService.getUserFavorites(user.getId())).thenReturn(List.of());

        mockMvc.perform(get("/favorites").with(authentication(authToken(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getFavoriteIds_authenticated_returns200() throws Exception {
        User user = testUser();
        UUID prodId = UUID.randomUUID();
        when(favoriteService.getFavoriteProductIds(user.getId())).thenReturn(Set.of(prodId));

        mockMvc.perform(get("/favorites/ids").with(authentication(authToken(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void addFavorite_authenticated_returns200() throws Exception {
        User user = testUser();
        UUID productId = UUID.randomUUID();
        doNothing().when(favoriteService).addFavorite(user.getId(), productId);

        mockMvc.perform(post("/favorites/{productId}", productId)
                        .with(csrf())
                        .with(authentication(authToken(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(favoriteService).addFavorite(user.getId(), productId);
    }

    @Test
    void addFavorite_duplicate_returns400() throws Exception {
        User user = testUser();
        UUID productId = UUID.randomUUID();
        doThrow(new RuntimeException("Product already in favorites"))
                .when(favoriteService).addFavorite(user.getId(), productId);

        mockMvc.perform(post("/favorites/{productId}", productId)
                        .with(csrf())
                        .with(authentication(authToken(user))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeFavorite_authenticated_returns200() throws Exception {
        User user = testUser();
        UUID productId = UUID.randomUUID();
        doNothing().when(favoriteService).removeFavorite(user.getId(), productId);

        mockMvc.perform(delete("/favorites/{productId}", productId)
                        .with(csrf())
                        .with(authentication(authToken(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(favoriteService).removeFavorite(user.getId(), productId);
    }

    @Test
    void checkFavorite_true_returns200WithTrue() throws Exception {
        User user = testUser();
        UUID productId = UUID.randomUUID();
        when(favoriteService.isFavorite(user.getId(), productId)).thenReturn(true);

        mockMvc.perform(get("/favorites/{productId}/check", productId)
                        .with(authentication(authToken(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void checkFavorite_false_returns200WithFalse() throws Exception {
        User user = testUser();
        UUID productId = UUID.randomUUID();
        when(favoriteService.isFavorite(user.getId(), productId)).thenReturn(false);

        mockMvc.perform(get("/favorites/{productId}/check", productId)
                        .with(authentication(authToken(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }
}
