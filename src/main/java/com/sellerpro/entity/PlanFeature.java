package com.sellerpro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "feature_key", nullable = false)
    private String featureKey;

    @Column(name = "feature_value")
    private String featureValue;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
