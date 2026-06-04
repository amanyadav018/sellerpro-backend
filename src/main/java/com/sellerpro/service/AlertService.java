package com.sellerpro.service;

import com.sellerpro.model.AlertRule;
import com.sellerpro.repository.AlertRuleRepository;
import com.sellerpro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRuleRepository alertRuleRepository;
    private final OrderRepository orderRepository;

    public List<AlertRule> getUserAlerts(String email) {
        return alertRuleRepository.findByUserEmail(email);
    }

    public AlertRule createAlert(String email, AlertRule alertRule) {
        alertRule.setUserEmail(email);
        alertRule.setCreatedAt(LocalDateTime.now());
        alertRule.setEnabled(true);
        return alertRuleRepository.save(alertRule);
    }

    public AlertRule updateAlert(String email, Long id, AlertRule updated) {
        AlertRule existing = alertRuleRepository.findByIdAndUserEmail(id, email)
            .orElseThrow(() -> new RuntimeException("Alert not found"));
        existing.setMetric(updated.getMetric());
        existing.setCondition(updated.getCondition());
        existing.setThreshold(updated.getThreshold());
        existing.setEnabled(updated.isEnabled());
        return alertRuleRepository.save(existing);
    }

    public void deleteAlert(String email, Long id) {
        alertRuleRepository.deleteByIdAndUserEmail(id, email);
    }

    public List<Map<String, Object>> getTriggeredAlerts(String email) {
        return alertRuleRepository.findTriggeredAlerts(email);
    }

    public void markAsRead(String email, Long id) {
        alertRuleRepository.markAlertRead(id, email);
    }

    // Called by DigestScheduler to evaluate alert rules
    public List<AlertRule> evaluateAlerts(String email) {
        List<AlertRule> rules = alertRuleRepository.findByUserEmailAndEnabled(email, true);
        List<AlertRule> triggered = new ArrayList<>();

        for (AlertRule rule : rules) {
            double currentValue = getCurrentMetricValue(email, rule.getMetric());
            boolean fires = switch (rule.getCondition()) {
                case "GT" -> currentValue > rule.getThreshold();
                case "LT" -> currentValue < rule.getThreshold();
                case "EQ" -> currentValue == rule.getThreshold();
                default -> false;
            };
            if (fires) triggered.add(rule);
        }
        return triggered;
    }

    private double getCurrentMetricValue(String email, String metric) {
        return switch (metric) {
            case "DAILY_ORDERS" -> orderRepository.countTodayOrders(email);
            case "DAILY_REVENUE" -> {
                java.math.BigDecimal rev = orderRepository.sumTodayRevenue(email);
                yield rev != null ? rev.doubleValue() : 0.0;
            }
            case "RETURN_RATE" -> orderRepository.calcReturnRate(email);
            default -> 0.0;
        };
    }
}
