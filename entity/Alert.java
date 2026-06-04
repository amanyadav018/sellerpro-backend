package com.sellerpro.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private String type; // LOW_STOCK, RETURN_SPIKE, REVENUE_DIP, HIGH_RETURNS

    @Column(nullable = false)
    private String severity; // INFO, WARNING, CRITICAL

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String platform;

    private BigDecimal value;

    private BigDecimal threshold;

    @Builder.Default
    private boolean read = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
