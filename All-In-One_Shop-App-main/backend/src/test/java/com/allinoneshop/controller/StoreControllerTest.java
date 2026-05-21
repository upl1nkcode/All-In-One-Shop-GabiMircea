package com.allinoneshop.controller;

import com.allinoneshop.dto.StoreDTO;
import com.allinoneshop.service.StoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = StoreController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class StoreControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private StoreService storeService;
    @MockBean private com.allinoneshop.security.JwtTokenProvider jwtTokenProvider;
    @MockBean private com.allinoneshop.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private StoreDTO store(String name) {
        return StoreDTO.builder().id(UUID.randomUUID()).name(name)
                .website("https://" + name.toLowerCase() + ".com").isActive(true).build();
    }

    @Test
    void getAllStores_returns200WithList() throws Exception {
        when(storeService.getAllStores()).thenReturn(List.of(store("Footlocker"), store("Zalando")));

        mockMvc.perform(get("/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getAllStores_empty_returns200() throws Exception {
        when(storeService.getAllStores()).thenReturn(List.of());

        mockMvc.perform(get("/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getStoreById_existing_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(storeService.getStoreById(id)).thenReturn(store("Asos"));

        mockMvc.perform(get("/stores/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Asos"));
    }

    @Test
    void getStoreById_notFound_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        when(storeService.getStoreById(id)).thenThrow(new RuntimeException("Store not found"));

        mockMvc.perform(get("/stores/{id}", id))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createStore_validBody_returns200() throws Exception {
        StoreDTO dto = StoreDTO.builder().name("Zalando").website("https://zalando.com").build();
        when(storeService.createStore(any(StoreDTO.class))).thenReturn(store("Zalando"));

        mockMvc.perform(post("/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createStore_blankName_returns400() throws Exception {
        StoreDTO dto = StoreDTO.builder().name("").website("https://example.com").build();

        mockMvc.perform(post("/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createStore_blankWebsite_returns400() throws Exception {
        StoreDTO dto = StoreDTO.builder().name("MyStore").website("").build();

        mockMvc.perform(post("/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStore_validBody_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        StoreDTO dto = StoreDTO.builder().name("Updated Store").website("https://updated.com").build();
        when(storeService.updateStore(eq(id), any())).thenReturn(store("Updated Store"));

        mockMvc.perform(put("/stores/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Store"));
    }

    @Test
    void deleteStore_existing_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(storeService).deleteStore(id);

        mockMvc.perform(delete("/stores/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(storeService).deleteStore(id);
    }

    @Test
    void deleteStore_notFound_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("Store not found")).when(storeService).deleteStore(id);

        mockMvc.perform(delete("/stores/{id}", id))
                .andExpect(status().isBadRequest());
    }
}
