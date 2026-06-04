package com.sellerpro.scheduler;

import com.sellerpro.dto.DashboardResponse;
import com.sellerpro.entity.Client;
import com.sellerpro.repository.ClientRepository;
import com.sellerpro.service.DashboardService;
import com.sellerpro.service.WhatsappService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DigestScheduler {

    private final WhatsappService whatsappService;
    private final DashboardService dashboardService;
    private final ClientRepository clientRepository;

    /**
     * Send weekly WhatsApp digest every Monday at 9:00 AM IST
     */
    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Kolkata")
    public void sendWeeklyDigests() {
        log.info("Sending weekly WhatsApp digests...");

        List<Client> activeClients = clientRepository.findByActiveTrueAndWhatsappNotNull();

        for (Client client : activeClients) {
            try {
                // Get last 7 days dashboard
                LocalDate end = LocalDate.now();
                LocalDate start = end.minusDays(7);

                DashboardResponse dashboard = dashboardService.getDashboard(client.getId(), start, end);
                whatsappService.sendWeeklyDigest(client.getWhatsapp(), dashboard);

                log.info("Digest sent to client {} ({})", client.getId(), client.getWhatsapp());
            } catch (Exception e) {
                log.error("Failed to send digest to client {}: {}", client.getId(), e.getMessage());
            }
        }

        log.info("Weekly digest send complete.");
    }
}
