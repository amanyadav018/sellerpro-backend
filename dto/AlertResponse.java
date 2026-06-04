package com.sellerpro.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private Long id;
    private String type;         // LOW_STOCK, RETURN_SPIKE, REVENUE_DIP, HIGH_RETURNS
    private String severity;     // INFO, WARNING, CRITICAL
    private String title;
    private String message;
    private String platform;
    private BigDecimal value;
    private BigDecimal threshold;
    private boolean read;
    private LocalDateTime createdAt;

    // Summary for dashboard
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertSummary {
        private int totalAlerts;
        private int criticalCount;
        private int warningCount;
        private int infoCount;
        private int unreadCount;
        private List<AlertResponse> recentAlerts;
    }
}
