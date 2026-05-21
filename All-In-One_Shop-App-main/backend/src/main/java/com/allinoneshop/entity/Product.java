package com.allinoneshop.entity;

import com.allinoneshop.entity.enums.Gender;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    private UUID id;
    private String name;
    private String description;
    private Brand brand;
    private Category category;
    private String imageUrl;
    private String[] additionalImages;
    private String[] sizes;
    private String[] colors;

    @Builder.Default
    private List<ProductPrice> prices = new ArrayList<>();

    private Gender gender;

    @Builder.Default
    private Boolean isActive = true;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
