package com.sellerpro.service;

import com.sellerpro.dto.AlertResponse;
import com.sellerpro.dto.DashboardResponse;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;

@Service
@Slf4j
public class WhatsappService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-from}")
    private String fromNumber; // "whatsapp:+14155238886"

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    /**
     * Send weekly digest to seller's WhatsApp
     */
    public void sendWeeklyDigest(String toPhone, DashboardResponse dashboard) {
        String message = buildWeeklyDigestMessage(dashboard);
        send(toPhone, message);
    }

    /**
     * Send critical alert via WhatsApp
     */
    public void sendCriticalAlert(String toPhone, AlertResponse alert) {
        String message = buildAlertMessage(alert);
        send(toPhone, message);
    }

    /**
     * Send custom message
     */
    public void send(String toPhone, String message) {
        try {
            String toWhatsapp = toPhone.startsWith("whatsapp:") ? toPhone : "whatsapp:" + toPhone;

            Message msg = Message.creator(
                new PhoneNumber(toWhatsapp),
                new PhoneNumber(fromNumber),
                message
            ).create();

            log.info("WhatsApp message sent to {}, SID: {}", toPhone, msg.getSid());
        } catch (Exception e) {
            log.error("Failed to send WhatsApp to {}: {}", toPhone, e.getMessage());
        }
    }

    private String buildWeeklyDigestMessage(DashboardResponse dashboard) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 *SellerPro Weekly Digest*\n\n");

        if (dashboard.getSummary() != null) {
            var s = dashboard.getSummary();
            sb.append("💰 *Revenue:* ₹").append(formatAmount(s.getTotalRevenue())).append("\n");
            sb.append("📦 *Orders:* ").append(s.getTotalOrders()).append("\n");
            sb.append("🔄 *Returns:* ").append(s.getTotalReturns()).append("\n");
            sb.append("📈 *Avg Order Value:* ₹").append(formatAmount(s.getAverageOrderValue())).append("\n\n");
        }

        sb.append("🏪 *Platform Breakdown:*\n");
        if (dashboard.getPlatformBreakdown() != null) {
            dashboard.getPlatformBreakdown().forEach(p -> {
                sb.append("• ").append(p.getPlatform())
                  .append(": ₹").append(formatAmount(p.getRevenue()))
                  .append(" (").append(p.getOrderCount()).append(" orders)\n");
            });
        }

        sb.append("\n_Sent by SellerPro_");
        return sb.toString();
    }

    private String buildAlertMessage(AlertResponse alert) {
        String emoji = switch (alert.getSeverity()) {
            case "CRITICAL" -> "🚨";
            case "WARNING" -> "⚠️";
            default -> "ℹ️";
        };

        return emoji + " *SellerPro Alert*\n\n" +
               "*" + alert.getTitle() + "*\n" +
               alert.getMessage() + "\n\n" +
               "_Check your SellerPro dashboard for details._";
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0";
        // Indian number format: 1,00,000
        long val = amount.longValue();
        if (val >= 100000) {
            return String.format("%.1fL", val / 100000.0);
        } else if (val >= 1000) {
            return String.format("%.1fK", val / 1000.0);
        }
        return String.valueOf(val);
    }
}
