package com.allinoneshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "search_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "search_query", nullable = false, length = 500)
    private String searchQuery;

    @Column(name = "results_count")
    @Builder.Default
    private Integer resultsCount = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
