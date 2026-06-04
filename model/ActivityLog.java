package com.sellerpro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String action; // LOGIN, LOGOUT, UPLOAD, EXPORT, PLAN_CHANGE, etc.

    private String detail;

    private String ipAddress;

    private String userAgent;

    // SUCCESS, FAILURE, WARNING
    private String result = "SUCCESS";

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
