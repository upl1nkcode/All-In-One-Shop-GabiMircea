package com.allinoneshop.service;

import com.allinoneshop.dto.StoreDTO;
import com.allinoneshop.entity.Store;
import com.allinoneshop.repository.ProductPriceRepository;
import com.allinoneshop.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;
    private final ProductPriceRepository priceRepository;

    public List<StoreDTO> getAllStores() {
        return storeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StoreDTO getStoreById(UUID id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found"));
        return convertToDTO(store);
    }

    public StoreDTO createStore(StoreDTO dto) {
        Store store = new Store();
        store.setName(dto.getName());
        store.setWebsite(dto.getWebsite());
        store.setLogoUrl(dto.getLogoUrl());
        store.setIsActive(true);
        store = storeRepository.save(store);
        return convertToDTO(store);
    }

    public StoreDTO updateStore(UUID id, StoreDTO dto) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found"));
        store.setName(dto.getName());
        store.setWebsite(dto.getWebsite());
        store.setLogoUrl(dto.getLogoUrl());
        store = storeRepository.save(store);
        return convertToDTO(store);
    }

    public void deleteStore(UUID id) {
        storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found"));
        priceRepository.deleteByStoreId(id);
        storeRepository.deleteById(id);
    }

    private StoreDTO convertToDTO(Store store) {
        return StoreDTO.builder()
                .id(store.getId())
                .name(store.getName())
                .website(store.getWebsite())
                .logoUrl(store.getLogoUrl())
                .isActive(store.getIsActive())
                .build();
    }
}
