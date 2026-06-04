package com.sellerpro.controller;

import com.sellerpro.dto.DashboardResponse;
import com.sellerpro.entity.Client;
import com.sellerpro.repository.ClientRepository;
import com.sellerpro.service.DashboardService;
import com.sellerpro.service.WhatsappService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/digest")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DigestController {

    private final WhatsappService whatsappService;
    private final DashboardService dashboardService;
    private final ClientRepository clientRepository;

    /**
     * POST /api/digest/send?clientId=1
     * Manually trigger WhatsApp digest for a client
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendDigest(@RequestParam Long clientId) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new RuntimeException("Client not found"));

        if (client.getWhatsapp() == null || client.getWhatsapp().isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Client has no WhatsApp number configured"));
        }

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);

        DashboardResponse dashboard = dashboardService.getDashboard(clientId, start, end);
        whatsappService.sendWeeklyDigest(client.getWhatsapp(), dashboard);

        return ResponseEntity.ok(Map.of("message", "Digest sent to " + client.getWhatsapp()));
    }

    /**
     * POST /api/digest/test?clientId=1&phone=+919999999999
     * Send test message to verify WhatsApp setup
     */
    @PostMapping("/test")
    public ResponseEntity<?> sendTest(@RequestParam Long clientId, @RequestParam String phone) {
        whatsappService.send(phone, "✅ *SellerPro WhatsApp Setup Successful!*\n\nYour weekly digests will be delivered here every Monday morning.");
        return ResponseEntity.ok(Map.of("message", "Test message sent to " + phone));
    }
}
