package com.sellerpro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String severity = "INFO";

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(length = 30)
    private String platform;

    private String sku;

    @Column(name = "metric_key")
    private String metricKey;

    @Column(name = "metric_value", precision = 15, scale = 2)
    private BigDecimal metricValue;

    @Column(name = "threshold_value", precision = 15, scale = 2)
    private BigDecimal thresholdValue;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "is_dismissed", nullable = false)
    @Builder.Default
    private Boolean isDismissed = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "auto_resolved", nullable = false)
    @Builder.Default
    private Boolean autoResolved = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Alert type constants
    public static final String TYPE_RETURN_SPIKE = "RETURN_SPIKE";
    public static final String TYPE_REVENUE_DROP = "REVENUE_DROP";
    public static final String TYPE_LOW_STOCK = "LOW_STOCK";
    public static final String TYPE_HIGH_COMMISSION = "HIGH_COMMISSION";
    public static final String TYPE_SUBSCRIPTION_EXPIRY = "SUBSCRIPTION_EXPIRY";
    public static final String TYPE_PAYMENT_PENDING = "PAYMENT_PENDING";

    // Severity constants
    public static final String SEVERITY_INFO = "INFO";
    public static final String SEVERITY_WARNING = "WARNING";
    public static final String SEVERITY_CRITICAL = "CRITICAL";
}
