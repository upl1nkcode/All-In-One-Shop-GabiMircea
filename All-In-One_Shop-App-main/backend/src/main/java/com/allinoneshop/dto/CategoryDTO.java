package com.allinoneshop.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private UUID id;
    private String name;
    private String slug;
}
