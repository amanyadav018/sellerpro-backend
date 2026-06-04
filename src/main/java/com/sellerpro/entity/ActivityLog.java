package com.sellerpro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 30)
    private String platform;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "records_processed")
    private Integer recordsProcessed;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "SUCCESS";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Common action constants
    public static final String ACTION_LOGIN = "USER_LOGIN";
    public static final String ACTION_LOGOUT = "USER_LOGOUT";
    public static final String ACTION_FILE_UPLOAD = "FILE_UPLOAD";
    public static final String ACTION_FILE_PARSE = "FILE_PARSE";
    public static final String ACTION_PLAN_CHANGE = "PLAN_CHANGE";
    public static final String ACTION_SUBSCRIPTION_CREATE = "SUBSCRIPTION_CREATE";
    public static final String ACTION_SUBSCRIPTION_CANCEL = "SUBSCRIPTION_CANCEL";
    public static final String ACTION_EXPORT = "DATA_EXPORT";
    public static final String ACTION_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String ACTION_PAYMENT_FAILED = "PAYMENT_FAILED";
}
