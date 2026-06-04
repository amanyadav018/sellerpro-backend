package com.sellerpro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds

    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String fullName;
        private String role;
        private String planName;
        private String planDisplayName;
        private String subscriptionStatus;
        private String subscriptionEndDate;
        private Boolean emailVerified;
        private Boolean onboardingComplete;
    }
}
