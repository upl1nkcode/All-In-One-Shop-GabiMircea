package com.allinoneshop.entity;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    private UUID id;
    private String name;
    private String slug;
    private Category parent;
    private OffsetDateTime createdAt;
}
