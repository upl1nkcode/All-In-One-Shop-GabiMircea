package com.allinoneshop.entity;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {

    private UUID id;
    private User user;
    private String searchQuery;

    @Builder.Default
    private Integer resultsCount = 0;

    private OffsetDateTime createdAt;
}
