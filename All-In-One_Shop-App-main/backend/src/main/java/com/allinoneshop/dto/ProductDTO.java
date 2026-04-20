package com.allinoneshop.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private String[] additionalImages;
    private String[] sizes;
    private String[] colors;
    private String gender;
    private Boolean isActive;
    private UUID brandId;
    private UUID categoryId;
    private BrandDTO brand;
    private CategoryDTO category;
    private List<ProductPriceDTO> prices;
    private BigDecimal lowestPrice;
    private BigDecimal highestPrice;
    private int storeCount;
}
