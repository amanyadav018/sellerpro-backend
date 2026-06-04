package com.sellerpro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    @Value("${whatsapp.api.url:https://api.wati.io/api/v1}")
    private String whatsappApiUrl;

    @Value("${whatsapp.api.token:}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean sendDigest(String phoneNumber, Map<String, Object> digestData) {
        try {
            String message = buildDigestMessage(digestData);
            return sendMessage(phoneNumber, message);
        } catch (Exception e) {
            log.error("WhatsApp digest failed for {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    public boolean sendAlert(String phoneNumber, String alertMessage) {
        try {
            return sendMessage(phoneNumber, "⚠️ *SellerPro Alert*\n\n" + alertMessage);
        } catch (Exception e) {
            log.error("WhatsApp alert failed for {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    private boolean sendMessage(String phoneNumber, String message) {
        if (apiToken.isEmpty()) {
            log.warn("WhatsApp token not configured — skipping send");
            return false;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        Map<String, Object> body = Map.of(
            "messaging_product", "whatsapp",
            "to", phoneNumber,
            "type", "text",
            "text", Map.of("body", message)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                whatsappApiUrl + "/messages", request, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("WhatsApp API error: {}", e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private String buildDigestMessage(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 *SellerPro Daily Digest*\n");
        sb.append("━━━━━━━━━━━━━━━━\n\n");

        if (data.containsKey("revenue")) {
            sb.append("💰 *Revenue:* ₹").append(data.get("revenue")).append("\n");
        }
        if (data.containsKey("orders")) {
            sb.append("📦 *Orders:* ").append(data.get("orders")).append("\n");
        }
        if (data.containsKey("returns")) {
            sb.append("↩️ *Returns:* ").append(data.get("returns")).append("\n");
        }
        if (data.containsKey("topPlatform")) {
            sb.append("🏆 *Top Platform:* ").append(data.get("topPlatform")).append("\n");
        }
        if (data.containsKey("alerts") && ((List<?>) data.get("alerts")).size() > 0) {
            sb.append("\n⚠️ *Active Alerts:* ").append(((List<?>) data.get("alerts")).size()).append("\n");
        }

        sb.append("\n_View details at sellerpro.in_");
        return sb.toString();
    }
}
