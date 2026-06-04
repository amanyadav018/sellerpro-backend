package com.sellerpro.scheduler;

import com.sellerpro.entity.Client;
import com.sellerpro.repository.ClientRepository;
import com.sellerpro.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private final AlertService alertService;
    private final ClientRepository clientRepository;

    /**
     * Run alert checks daily at 8:00 AM IST
     * cron = second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Kolkata")
    public void runDailyAlerts() {
        log.info("Running scheduled daily alert checks...");

        List<Client> activeClients = clientRepository.findByActiveTrue();
        log.info("Checking alerts for {} active clients", activeClients.size());

        for (Client client : activeClients) {
            try {
                var alerts = alertService.runChecks(client.getId());
                if (!alerts.isEmpty()) {
                    log.info("Generated {} new alerts for client {}", alerts.size(), client.getId());
                }
            } catch (Exception e) {
                log.error("Error running alerts for client {}: {}", client.getId(), e.getMessage());
            }
        }

        log.info("Daily alert checks completed.");
    }
}
