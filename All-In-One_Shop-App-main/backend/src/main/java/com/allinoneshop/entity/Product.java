package com.allinoneshop.entity;

import jakarta.persistence.*;
import lombok.*;
import com.allinoneshop.entity.enums.Gender;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "image_url")
    private String imageUrl;

    // Stored as comma-separated JSON strings in TEXT columns
    @Column(name = "additional_images", columnDefinition = "TEXT")
    private String additionalImagesRaw;

    @Column(columnDefinition = "TEXT")
    private String sizesRaw;

    @Column(columnDefinition = "TEXT")
    private String colorsRaw;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductPrice> prices = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Transient array accessors for backward compatibility with services
    @Transient
    public String[] getAdditionalImages() {
        return splitCsv(additionalImagesRaw);
    }

    public void setAdditionalImages(String[] images) {
        this.additionalImagesRaw = joinCsv(images);
    }

    @Transient
    public String[] getSizes() {
        return splitCsv(sizesRaw);
    }

    public void setSizes(String[] sizes) {
        this.sizesRaw = joinCsv(sizes);
    }

    @Transient
    public String[] getColors() {
        return splitCsv(colorsRaw);
    }

    public void setColors(String[] colors) {
        this.colorsRaw = joinCsv(colors);
    }

    private static String[] splitCsv(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        return raw.split("\\|\\|");
    }

    private static String joinCsv(String[] arr) {
        if (arr == null || arr.length == 0) return null;
        return String.join("||", arr);
    }
}
