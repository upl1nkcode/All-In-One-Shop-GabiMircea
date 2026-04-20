package com.allinoneshop.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPriceDTO {
    private UUID id;
    private StoreDTO store;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String currency;
    private String productUrl;
    private Boolean inStock;
    private BigDecimal savings;
}
