package com.allinoneshop.entity;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    private UUID id;
    private String name;
    private String website;
    private String logoUrl;

    @Builder.Default
    private Boolean isActive = true;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
