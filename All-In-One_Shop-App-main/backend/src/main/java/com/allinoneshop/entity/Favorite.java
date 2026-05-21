package com.allinoneshop.entity;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {

    private UUID id;
    private User user;
    private Product product;
    private OffsetDateTime createdAt;
}
