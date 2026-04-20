package com.allinoneshop.entity;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {

    private UUID id;
    private String name;
    private String logoUrl;
    private OffsetDateTime createdAt;
}
