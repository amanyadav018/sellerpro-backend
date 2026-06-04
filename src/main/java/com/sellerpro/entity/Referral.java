package com.sellerpro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "referrals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_user_id", nullable = false)
    private User referrer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_user_id")
    private User referred;

    @Column(name = "referral_code", nullable = false, unique = true, length = 20)
    private String referralCode;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "reward_months", nullable = false)
    @Builder.Default
    private Integer rewardMonths = 1;

    @Column(name = "reward_applied", nullable = false)
    @Builder.Default
    private Boolean rewardApplied = false;

    @Column(name = "reward_applied_at")
    private LocalDateTime rewardAppliedAt;

    @Column(name = "referred_email")
    private String referredEmail;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONVERTED = "CONVERTED";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_REWARDED = "REWARDED";
}
