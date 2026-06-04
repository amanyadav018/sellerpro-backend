package com.sellerpro.service;

import com.sellerpro.dto.AlertResponse;
import com.sellerpro.entity.Alert;
import com.sellerpro.entity.Order;
import com.sellerpro.repository.AlertRepository;
import com.sellerpro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final OrderRepository orderRepository;

    // Thresholds
    private static final double RETURN_SPIKE_THRESHOLD = 0.20;   // 20% return rate
    private static final double REVENUE_DIP_THRESHOLD = 0.30;    // 30% drop week-over-week
    private static final double HIGH_RETURN_RATE = 0.15;         // 15% returns on a platform

    /**
     * Run all checks for a client and generate alerts
     */
    @Transactional
    public List<AlertResponse> runChecks(Long clientId) {
        log.info("Running alert checks for client {}", clientId);
        List<Alert> newAlerts = new ArrayList<>();

        newAlerts.addAll(checkReturnSpike(clientId));
        newAlerts.addAll(checkRevenueDip(clientId));
        newAlerts.addAll(checkHighReturnsByPlatform(clientId));

        List<Alert> saved = alertRepository.saveAll(newAlerts);
        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private List<Alert> checkReturnSpike(Long clientId) {
        List<Alert> alerts = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        LocalDate twoWeeksAgo = today.minusDays(14);

        List<Order> thisWeek = orderRepository.findByClientIdAndOrderDateBetween(clientId, weekAgo, today);
        List<Order> lastWeek = orderRepository.findByClientIdAndOrderDateBetween(clientId, twoWeeksAgo, weekAgo);

        if (thisWeek.isEmpty() || lastWeek.isEmpty()) return alerts;

        long thisWeekReturns = thisWeek.stream().filter(o -> "RETURNED".equals(o.getStatus())).count();
        long lastWeekReturns = lastWeek.stream().filter(o -> "RETURNED".equals(o.getStatus())).count();

        double thisWeekReturnRate = (double) thisWeekReturns / thisWeek.size();
        double lastWeekReturnRate = lastWeek.isEmpty() ? 0 : (double) lastWeekReturns / lastWeek.size();

        if (thisWeekReturnRate > RETURN_SPIKE_THRESHOLD
                && thisWeekReturnRate > lastWeekReturnRate * 1.5
                && !alreadyFiredToday(clientId, "RETURN_SPIKE")) {

            String severity = thisWeekReturnRate > 0.30 ? "CRITICAL" : "WARNING";
            alerts.add(Alert.builder()
                .clientId(clientId)
                .type("RETURN_SPIKE")
                .severity(severity)
                .title("Return Rate Spike Detected")
                .message(String.format(
                    "This week's return rate is %.1f%% (last week: %.1f%%). Check product quality or listing accuracy.",
                    thisWeekReturnRate * 100, lastWeekReturnRate * 100))
                .value(BigDecimal.valueOf(thisWeekReturnRate * 100).setScale(1, RoundingMode.HALF_UP))
                .threshold(BigDecimal.valueOf(RETURN_SPIKE_THRESHOLD * 100))
                .build());
        }

        return alerts;
    }

    private List<Alert> checkRevenueDip(Long clientId) {
        List<Alert> alerts = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        LocalDate twoWeeksAgo = today.minusDays(14);

        List<Order> thisWeek = orderRepository.findByClientIdAndOrderDateBetween(clientId, weekAgo, today);
        List<Order> lastWeek = orderRepository.findByClientIdAndOrderDateBetween(clientId, twoWeeksAgo, weekAgo);

        BigDecimal thisWeekRevenue = thisWeek.stream()
            .map(o -> o.getSaleAmount() != null ? o.getSaleAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lastWeekRevenue = lastWeek.stream()
            .map(o -> o.getSaleAmount() != null ? o.getSaleAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (lastWeekRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal dropPct = lastWeekRevenue.subtract(thisWeekRevenue)
                .divide(lastWeekRevenue, 4, RoundingMode.HALF_UP);

            if (dropPct.doubleValue() > REVENUE_DIP_THRESHOLD
                    && !alreadyFiredToday(clientId, "REVENUE_DIP")) {

                String severity = dropPct.doubleValue() > 0.50 ? "CRITICAL" : "WARNING";
                alerts.add(Alert.builder()
                    .clientId(clientId)
                    .type("REVENUE_DIP")
                    .severity(severity)
                    .title("Revenue Drop Alert")
                    .message(String.format(
                        "Revenue dropped %.1f%% this week (₹%.0f vs ₹%.0f last week). Investigate listing issues or ad spend.",
                        dropPct.doubleValue() * 100, thisWeekRevenue, lastWeekRevenue))
                    .value(thisWeekRevenue)
                    .threshold(lastWeekRevenue)
                    .build());
            }
        }

        return alerts;
    }

    private List<Alert> checkHighReturnsByPlatform(Long clientId) {
        List<Alert> alerts = new ArrayList<>();
        String[] platforms = {"AMAZON", "FLIPKART", "MEESHO", "NYKAA", "WEBSITE"};

        LocalDate monthAgo = LocalDate.now().minusDays(30);

        for (String platform : platforms) {
            List<Order> orders = orderRepository.findByClientIdAndPlatformAndOrderDateBetween(
                clientId, platform, monthAgo, LocalDate.now());

            if (orders.size() < 10) continue; // not enough data

            long returns = orders.stream().filter(o -> "RETURNED".equals(o.getStatus())).count();
            double returnRate = (double) returns / orders.size();

            if (returnRate > HIGH_RETURN_RATE
                    && !alreadyFiredToday(clientId, "HIGH_RETURNS_" + platform)) {

                alerts.add(Alert.builder()
                    .clientId(clientId)
                    .type("HIGH_RETURNS")
                    .severity("WARNING")
                    .title(platform + " High Return Rate")
                    .message(String.format(
                        "%s return rate is %.1f%% over last 30 days (%d returns out of %d orders). Review product listings.",
                        platform, returnRate * 100, returns, orders.size()))
                    .platform(platform)
                    .value(BigDecimal.valueOf(returnRate * 100).setScale(1, RoundingMode.HALF_UP))
                    .threshold(BigDecimal.valueOf(HIGH_RETURN_RATE * 100))
                    .build());
            }
        }

        return alerts;
    }

    private boolean alreadyFiredToday(Long clientId, String type) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return alertRepository.existsByClientIdAndTypeAndCreatedAtAfter(clientId, type, since);
    }

    /**
     * Get all alerts for a client
     */
    public AlertResponse.AlertSummary getAlerts(Long clientId) {
        List<Alert> all = alertRepository.findByClientIdOrderByCreatedAtDesc(clientId);
        long unread = alertRepository.countByClientIdAndReadFalse(clientId);

        List<AlertResponse> responses = all.stream()
            .limit(50)
            .map(this::toResponse)
            .collect(Collectors.toList());

        return AlertResponse.AlertSummary.builder()
            .totalAlerts(all.size())
            .criticalCount((int) all.stream().filter(a -> "CRITICAL".equals(a.getSeverity())).count())
            .warningCount((int) all.stream().filter(a -> "WARNING".equals(a.getSeverity())).count())
            .infoCount((int) all.stream().filter(a -> "INFO".equals(a.getSeverity())).count())
            .unreadCount((int) unread)
            .recentAlerts(responses)
            .build();
    }

    @Transactional
    public void markAllRead(Long clientId) {
        alertRepository.markAllAsRead(clientId);
    }

    @Transactional
    public void markRead(Long alertId, Long clientId) {
        alertRepository.markAsRead(alertId, clientId);
    }

    private AlertResponse toResponse(Alert a) {
        return AlertResponse.builder()
            .id(a.getId())
            .type(a.getType())
            .severity(a.getSeverity())
            .title(a.getTitle())
            .message(a.getMessage())
            .platform(a.getPlatform())
            .value(a.getValue())
            .threshold(a.getThreshold())
            .read(a.isRead())
            .createdAt(a.getCreatedAt())
            .build();
    }
}
