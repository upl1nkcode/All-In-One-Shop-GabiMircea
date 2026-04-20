package com.allinoneshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreDTO {
    private UUID id;

    @NotBlank(message = "Store name is required")
    @Size(min = 1, max = 255, message = "Store name must be between 1 and 255 characters")
    private String name;

    @NotBlank(message = "Website is required")
    @Size(max = 500, message = "Website URL cannot exceed 500 characters")
    private String website;

    @Size(max = 500, message = "Logo URL cannot exceed 500 characters")
    private String logoUrl;

    private Boolean isActive;
}
