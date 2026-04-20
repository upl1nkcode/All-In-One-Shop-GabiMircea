package com.allinoneshop.service;

import com.allinoneshop.dto.ProductDTO;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeilisearchService {

    private final Client meilisearchClient;
    private final ObjectMapper objectMapper;
    private static final String PRODUCTS_INDEX = "products";

    @PostConstruct
    public void initIndex() {
        try {
            Index index = meilisearchClient.index(PRODUCTS_INDEX);
            
            // Configure searchable attributes
            index.updateSearchableAttributesSettings(new String[]{
                "name", 
                "description", 
                "brand", 
                "category"
            });
            
            // Configure filterable attributes
            index.updateFilterableAttributesSettings(new String[]{
                "category", 
                "brand", 
                "gender",
                "price", 
                "isActive"
            });
            
            // Configure sortable attributes
            index.updateSortableAttributesSettings(new String[]{
                "price", 
                "createdAt"
            });
            
            log.info("Meilisearch index '{}' configured successfully", PRODUCTS_INDEX);
        } catch (MeilisearchException e) {
            log.warn("Failed to initialize Meilisearch index. Make sure Meilisearch is running. Error: {}", e.getMessage());
        }
    }

    public void indexProduct(ProductDTO product) {
        try {
            Index index = meilisearchClient.index(PRODUCTS_INDEX);
            
            Map<String, Object> document = new HashMap<>();
            document.put("id", product.getId().toString());
            document.put("name", product.getName());
            document.put("description", product.getDescription());
            document.put("brand", product.getBrand() != null ? product.getBrand().getName() : null);
            document.put("category", product.getCategory() != null ? product.getCategory().getName() : null);
            document.put("gender", product.getGender());
            document.put("price", product.getLowestPrice() != null ? product.getLowestPrice().doubleValue() : 0.0);
            document.put("isActive", product.getIsActive() != null ? product.getIsActive() : true);
            document.put("imageUrl", product.getImageUrl());
            
            index.addDocuments(objectMapper.writeValueAsString(List.of(document)));
            log.debug("Indexed product: {}", product.getId());
        } catch (Exception e) {
            log.error("Failed to index product in Meilisearch: {}", product.getId(), e);
        }
    }

    public void removeProduct(String productId) {
        try {
            Index index = meilisearchClient.index(PRODUCTS_INDEX);
            index.deleteDocument(productId);
            log.debug("Removed product from index: {}", productId);
        } catch (MeilisearchException e) {
            log.error("Failed to remove product from Meilisearch: {}", productId, e);
        }
    }
}
