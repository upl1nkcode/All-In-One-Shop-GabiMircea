package com.allinoneshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_prices", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "store_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(length = 3)
    @Builder.Default
    private String currency = "EUR";

    @Column(name = "product_url", nullable = false, length = 1000)
    private String productUrl;

    @Column(name = "in_stock")
    @Builder.Default
    private Boolean inStock = true;

    @UpdateTimestamp
    @Column(name = "last_checked")
    private OffsetDateTime lastChecked;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
