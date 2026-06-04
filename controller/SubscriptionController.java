package com.sellerpro.controller;

import com.sellerpro.model.Subscription;
import com.sellerpro.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // Current user's subscription info
    @GetMapping("/my")
    public ResponseEntity<?> getMySubscription(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            subscriptionService.getActiveSubscription(userDetails.getUsername())
                .map(s -> Map.of(
                    "plan", s.getPlan().getName(),
                    "displayName", s.getPlan().getDisplayName(),
                    "status", s.getStatus(),
                    "startDate", s.getStartDate(),
                    "endDate", s.getEndDate(),
                    "features", Map.of(
                        "analyticsAccess", s.getPlan().isAnalyticsAccess(),
                        "whatsappDigest", s.getPlan().isWhatsappDigest(),
                        "smartAlerts", s.getPlan().isSmartAlerts(),
                        "gstReportV2", s.getPlan().isGstReportV2()
                    )
                ))
                .orElse(Map.of("plan", "BASIC", "status", "NONE"))
        );
    }

    // Admin — assign plan to any user
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Subscription> assignPlan(
            @RequestParam String email,
            @RequestParam String plan,
            @RequestParam(defaultValue = "1") int months) {
        return ResponseEntity.ok(subscriptionService.assignPlan(email, plan, months));
    }

    // Admin — extend subscription
    @PostMapping("/extend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Subscription> extendSubscription(
            @RequestParam String email,
            @RequestParam(defaultValue = "1") int months) {
        return ResponseEntity.ok(subscriptionService.extendSubscription(email, months));
    }

    // Admin — suspend
    @PostMapping("/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> suspendSubscription(@RequestParam String email) {
        subscriptionService.suspendSubscription(email);
        return ResponseEntity.ok().build();
    }

    // Admin — cancel
    @PostMapping("/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelSubscription(@RequestParam String email) {
        subscriptionService.cancelSubscription(email);
        return ResponseEntity.ok().build();
    }

    // Admin — list all
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptionsForAdmin());
    }
}
