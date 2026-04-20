package com.allinoneshop.service;

import com.allinoneshop.dto.StoreDTO;
import com.allinoneshop.repository.ProductPriceRepository;
import com.allinoneshop.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class StoreServiceTest {

    private StoreRepository storeRepository;
    private ProductPriceRepository priceRepository;
    private StoreService storeService;

    @BeforeEach
    void setUp() {
        storeRepository = new StoreRepository();
        priceRepository = new ProductPriceRepository();
        storeService = new StoreService(storeRepository, priceRepository);
    }

    private StoreDTO buildDto(String name, String website) {
        StoreDTO dto = new StoreDTO();
        dto.setName(name);
        dto.setWebsite(website);
        return dto;
    }

    @Test
    void createStore_savesAndReturnsDTO() {
        StoreDTO result = storeService.createStore(buildDto("TestStore", "https://test.com"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("TestStore");
        assertThat(result.getWebsite()).isEqualTo("https://test.com");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void getAllStores_returnsAll() {
        storeService.createStore(buildDto("Store1", "https://s1.com"));
        storeService.createStore(buildDto("Store2", "https://s2.com"));

        List<StoreDTO> result = storeService.getAllStores();
        assertThat(result).hasSize(2);
    }

    @Test
    void getStoreById_found_returnsDTO() {
        StoreDTO created = storeService.createStore(buildDto("Find Me", "https://f.com"));

        StoreDTO result = storeService.getStoreById(created.getId());
        assertThat(result.getName()).isEqualTo("Find Me");
    }

    @Test
    void getStoreById_notFound_throwsException() {
        assertThatThrownBy(() -> storeService.getStoreById(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Store not found");
    }

    @Test
    void updateStore_existingStore_updatesFields() {
        StoreDTO created = storeService.createStore(buildDto("Old", "https://old.com"));

        StoreDTO result = storeService.updateStore(created.getId(), buildDto("New", "https://new.com"));

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getWebsite()).isEqualTo("https://new.com");
    }

    @Test
    void updateStore_notFound_throwsException() {
        assertThatThrownBy(() -> storeService.updateStore(UUID.randomUUID(), buildDto("x", "y")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Store not found");
    }

    @Test
    void deleteStore_existingStore_removesIt() {
        StoreDTO created = storeService.createStore(buildDto("Delete Me", "https://d.com"));

        storeService.deleteStore(created.getId());

        assertThat(storeRepository.count()).isZero();
    }

    @Test
    void deleteStore_notFound_throwsException() {
        assertThatThrownBy(() -> storeService.deleteStore(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Store not found");
    }
}
