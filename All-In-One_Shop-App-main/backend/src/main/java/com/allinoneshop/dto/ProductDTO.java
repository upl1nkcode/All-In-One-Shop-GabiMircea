package com.allinoneshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 500, message = "Product name must be between 1 and 500 characters")
    private String name;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
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
