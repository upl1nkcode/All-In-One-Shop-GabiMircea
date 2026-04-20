package com.allinoneshop.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandDTO {
    private UUID id;
    private String name;
    private String logoUrl;
}
