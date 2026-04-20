package com.allinoneshop.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreDTO {
    private UUID id;
    private String name;
    private String website;
    private String logoUrl;
    private Boolean isActive;
}
