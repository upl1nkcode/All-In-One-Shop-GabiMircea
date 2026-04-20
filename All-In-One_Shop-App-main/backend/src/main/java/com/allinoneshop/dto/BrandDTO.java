package com.allinoneshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandDTO {
    private UUID id;

    @NotBlank(message = "Brand name is required")
    @Size(min = 1, max = 255, message = "Brand name must be between 1 and 255 characters")
    private String name;

    @Size(max = 500, message = "Logo URL cannot exceed 500 characters")
    private String logoUrl;
}
