package com.sellerpro.service;

import com.sellerpro.dto.ReferralResponse;
import com.sellerpro.entity.Client;
import com.sellerpro.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralService {

    private final ClientRepository clientRepository;

    @Value("${app.base-url:https://app.sellerpro.in}")
    private String baseUrl;

    private static final BigDecimal COMMISSION_PER_REFERRAL = new BigDecimal("500"); // ₹500 per referral

    /**
     * Get or generate referral code for a client
     */
    public ReferralResponse getReferralInfo(Long clientId) {
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new RuntimeException("Client not found"));

        // Generate referral code if not exists
        if (client.getReferralCode() == null || client.getReferralCode().isBlank()) {
            client.setReferralCode(generateCode(client));
            clientRepository.save(client);
        }

        String referralLink = baseUrl + "/signup?ref=" + client.getReferralCode();

        // Get referrals (clients who signed up with this code)
        List<Client> referredClients = clientRepository.findByReferredBy(client.getReferralCode());

        List<ReferralResponse.ReferralItem> items = referredClients.stream()
            .map(ref -> ReferralResponse.ReferralItem.builder()
                .referredClientId(ref.getId())
                .referredName(ref.getBusinessName() != null ? ref.getBusinessName() : ref.getName())
                .signedUpAt(ref.getCreatedAt())
                .status(ref.isActive() ? "ACTIVE" : "PENDING")
                .earned(ref.isActive() ? COMMISSION_PER_REFERRAL : BigDecimal.ZERO)
                .build())
            .toList();

        long activeCount = items.stream().filter(i -> "ACTIVE".equals(i.getStatus())).count();
        BigDecimal totalEarned = COMMISSION_PER_REFERRAL.multiply(BigDecimal.valueOf(activeCount));

        return ReferralResponse.builder()
            .clientId(clientId)
            .referralCode(client.getReferralCode())
            .referralLink(referralLink)
            .totalReferrals(items.size())
            .activeReferrals((int) activeCount)
            .totalEarned(totalEarned)
            .pendingEarnings(COMMISSION_PER_REFERRAL.multiply(
                BigDecimal.valueOf(items.size() - activeCount)))
            .referrals(items)
            .build();
    }

    /**
     * Apply referral code when a new client signs up
     */
    public boolean applyReferralCode(Long newClientId, String referralCode) {
        if (referralCode == null || referralCode.isBlank()) return false;

        Client referrer = clientRepository.findByReferralCode(referralCode);
        if (referrer == null) {
            log.warn("Invalid referral code: {}", referralCode);
            return false;
        }

        Client newClient = clientRepository.findById(newClientId)
            .orElseThrow(() -> new RuntimeException("Client not found"));

        newClient.setReferredBy(referralCode);
        clientRepository.save(newClient);

        log.info("Client {} referred by {} (code: {})", newClientId, referrer.getId(), referralCode);
        return true;
    }

    private String generateCode(Client client) {
        // Use business name prefix + random suffix
        String prefix = client.getBusinessName() != null
            ? client.getBusinessName().replaceAll("[^A-Za-z0-9]", "").toUpperCase().substring(0, Math.min(4, client.getBusinessName().length()))
            : "SP";
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + suffix;
    }
}
