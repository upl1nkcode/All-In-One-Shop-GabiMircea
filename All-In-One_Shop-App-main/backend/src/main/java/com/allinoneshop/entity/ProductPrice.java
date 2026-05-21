package com.allinoneshop.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPrice {

    private UUID id;
    private Product product;
    private Store store;
    private BigDecimal price;
    private BigDecimal originalPrice;

    @Builder.Default
    private String currency = "EUR";

    private String productUrl;

    @Builder.Default
    private Boolean inStock = true;

    private OffsetDateTime lastChecked;
    private OffsetDateTime createdAt;
}
