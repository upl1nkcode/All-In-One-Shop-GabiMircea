package com.allinoneshop.service;

import com.allinoneshop.dto.StoreDTO;
import com.allinoneshop.entity.Store;
import com.allinoneshop.repository.ProductPriceRepository;
import com.allinoneshop.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock private StoreRepository storeRepository;
    @Mock private ProductPriceRepository priceRepository;
    @InjectMocks private StoreService storeService;

    private StoreDTO buildDto(String name, String website) {
        StoreDTO dto = new StoreDTO();
        dto.setName(name);
        dto.setWebsite(website);
        return dto;
    }

    @Test
    void createStore_savesAndReturnsDTO() {
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> {
            Store s = inv.getArgument(0); s.setId(UUID.randomUUID()); return s;
        });

        StoreDTO result = storeService.createStore(buildDto("TestStore", "https://test.com"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("TestStore");
        assertThat(result.getWebsite()).isEqualTo("https://test.com");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void getAllStores_returnsAll() {
        when(storeRepository.findAll()).thenReturn(List.of(
                Store.builder().id(UUID.randomUUID()).name("Store1").isActive(true).build(),
                Store.builder().id(UUID.randomUUID()).name("Store2").isActive(true).build()));

        List<StoreDTO> result = storeService.getAllStores();
        assertThat(result).hasSize(2);
    }

    @Test
    void getStoreById_found_returnsDTO() {
        UUID id = UUID.randomUUID();
        when(storeRepository.findById(id))
                .thenReturn(Optional.of(Store.builder().id(id).name("Find Me").isActive(true).build()));

        StoreDTO result = storeService.getStoreById(id);
        assertThat(result.getName()).isEqualTo("Find Me");
    }

    @Test
    void getStoreById_notFound_throwsException() {
        when(storeRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> storeService.getStoreById(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Store not found");
    }

    @Test
    void updateStore_existingStore_updatesFields() {
        UUID id = UUID.randomUUID();
        Store existing = Store.builder().id(id).name("Old").website("https://old.com").isActive(true).build();
        when(storeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(storeRepository.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

        StoreDTO result = storeService.updateStore(id, buildDto("New", "https://new.com"));
        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getWebsite()).isEqualTo("https://new.com");
    }

    @Test
    void updateStore_notFound_throwsException() {
        when(storeRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> storeService.updateStore(UUID.randomUUID(), buildDto("x", "y")))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Store not found");
    }

    @Test
    void deleteStore_existingStore_removesIt() {
        UUID id = UUID.randomUUID();
        when(storeRepository.findById(id))
                .thenReturn(Optional.of(Store.builder().id(id).name("Delete Me").isActive(true).build()));

        storeService.deleteStore(id);
        verify(priceRepository).deleteByStoreId(id);
        verify(storeRepository).deleteById(id);
    }

    @Test
    void deleteStore_notFound_throwsException() {
        when(storeRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> storeService.deleteStore(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Store not found");
    }
}
