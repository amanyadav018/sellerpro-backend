package com.sellerpro.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralResponse {

    private Long clientId;
    private String referralCode;
    private String referralLink;

    // Stats
    private int totalReferrals;
    private int activeReferrals;
    private BigDecimal totalEarned;
    private BigDecimal pendingEarnings;

    // List of referrals
    private List<ReferralItem> referrals;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferralItem {
        private Long referredClientId;
        private String referredName;
        private LocalDateTime signedUpAt;
        private String status;       // PENDING, ACTIVE, EXPIRED
        private BigDecimal earned;   // commission earned from this referral
    }
}
