package com.allinoneshop.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {
    private String query;
    private List<UUID> brandIds;
    private List<UUID> categoryIds;
    private List<UUID> storeIds;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> sizes;
    private List<String> colors;
    private String sortBy; // price_asc, price_desc, name_asc, name_desc
    private Integer page = 0;
    private Integer size = 20;
}
