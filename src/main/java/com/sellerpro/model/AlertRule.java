package com.sellerpro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alert_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    // DAILY_ORDERS, DAILY_REVENUE, RETURN_RATE
    @Column(nullable = false)
    private String metric;

    // GT, LT, EQ
    @Column(nullable = false)
    private String condition;

    @Column(nullable = false)
    private double threshold;

    private boolean enabled = true;

    // WHATSAPP, EMAIL, BOTH
    private String notifyVia = "WHATSAPP";

    private LocalDateTime createdAt;
    private LocalDateTime lastTriggeredAt;

    @Column(name = "is_read", columnDefinition = "boolean default false")
    private boolean read = false;
}
