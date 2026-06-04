package com.sellerpro.scheduler;

import com.sellerpro.model.AlertRule;
import com.sellerpro.repository.UserRepository;
import com.sellerpro.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DigestScheduler {

    private final UserRepository userRepository;
    private final WhatsAppService whatsAppService;
    private final AlertService alertService;
    private final MonthComparisonService monthComparisonService;
    private final ChannelCompareService channelCompareService;
    private final SkuRankingService skuRankingService;

    // Daily digest at 8 AM IST (2:30 UTC)
    @Scheduled(cron = "0 30 2 * * *", zone = "UTC")
    public void sendDailyDigests() {
        log.info("Starting daily WhatsApp digest job");

        List<String> proUserEmails = userRepository.findEmailsByPlanAndWhatsappEnabled("PRO");
        log.info("Sending digest to {} PRO users", proUserEmails.size());

        for (String email : proUserEmails) {
            try {
                sendDigestToUser(email);
            } catch (Exception e) {
                log.error("Digest failed for {}: {}", email, e.getMessage());
            }
        }
    }

    // Alert evaluation every 6 hours
    @Scheduled(cron = "0 0 */6 * * *")
    public void evaluateAlerts() {
        log.info("Evaluating alert rules for PRO users");

        List<String> proUserEmails = userRepository.findEmailsByPlanAndWhatsappEnabled("PRO");

        for (String email : proUserEmails) {
            try {
                List<AlertRule> triggered = alertService.evaluateAlerts(email);
                String phone = userRepository.findPhoneByEmail(email);

                for (AlertRule alert : triggered) {
                    String msg = String.format("Alert triggered! %s %s %.2f",
                        alert.getMetric(), alert.getCondition(), alert.getThreshold());
                    whatsAppService.sendAlert(phone, msg);
                    log.info("Alert sent to {} for rule {}", email, alert.getId());
                }
            } catch (Exception e) {
                log.error("Alert eval failed for {}: {}", email, e.getMessage());
            }
        }
    }

    private void sendDigestToUser(String email) {
        String phone = userRepository.findPhoneByEmail(email);
        if (phone == null || phone.isBlank()) return;

        Map<String, Object> digestData = Map.of(
            "revenue", getTodayRevenue(email),
            "orders", skuRankingService.getTotalOrders(email),
            "topPlatform", channelCompareService.getTopPlatform(email),
            "growth", monthComparisonService.getGrowthPercent(email),
            "alerts", alertService.getTriggeredAlerts(email)
        );

        whatsAppService.sendDigest(phone, digestData);
        log.info("Digest sent to {}", email);
    }

    private double getTodayRevenue(String email) {
        // Placeholder — OrderRepository.sumTodayRevenue wired through AlertService
        return 0.0;
    }
}
