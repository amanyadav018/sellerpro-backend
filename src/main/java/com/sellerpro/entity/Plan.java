package com.sellerpro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_months", nullable = false)
    @Builder.Default
    private Integer durationMonths = 1;

    @Column(name = "max_months_history")
    private Integer maxMonthsHistory; // null = unlimited

    @Column(name = "max_platforms", nullable = false)
    @Builder.Default
    private Integer maxPlatforms = 1;

    @Column(name = "whatsapp_digest", nullable = false)
    @Builder.Default
    private Boolean whatsappDigest = false;

    @Column(name = "analytics_access", nullable = false)
    @Builder.Default
    private Boolean analyticsAccess = false;

    @Column(name = "gst_report_v2", nullable = false)
    @Builder.Default
    private Boolean gstReportV2 = false;

    @Column(name = "smart_alerts", nullable = false)
    @Builder.Default
    private Boolean smartAlerts = false;

    @Column(name = "csv_export", nullable = false)
    @Builder.Default
    private Boolean csvExport = true;

    @Column(name = "referral_program", nullable = false)
    @Builder.Default
    private Boolean referralProgram = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<PlanFeature> features = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "plan_allowed_platforms", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "platform")
    @Builder.Default
    private List<String> allowedPlatforms = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPlatformAllowed(String platform) {
        return allowedPlatforms != null && allowedPlatforms.contains(platform.toUpperCase());
    }

    public boolean isUnlimitedHistory() {
        return maxMonthsHistory == null;
    }
}
