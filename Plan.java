package com.sellerpro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // BASIC, PRO, CUSTOM

    private String displayName; // "Basic Plan", "Pro Plan"

    private double price; // monthly price in INR

    private String description;

    // Comma-separated: AMAZON,FLIPKART,MEESHO,NYKAA,WEBSITE
    private String allowedPlatforms;

    private int maxMonthsHistory; // -1 = unlimited

    private boolean analyticsAccess;
    private boolean whatsappDigest;
    private boolean smartAlerts;
    private boolean gstReportV2;
    private boolean referralBonus;

    private boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
