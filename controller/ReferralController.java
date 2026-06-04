package com.sellerpro.controller;

import com.sellerpro.dto.ReferralResponse;
import com.sellerpro.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/referral")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReferralController {

    private final ReferralService referralService;

    /**
     * GET /api/referral?clientId=1
     * Returns referral code, link, and stats
     */
    @GetMapping
    public ResponseEntity<ReferralResponse> getReferralInfo(@RequestParam Long clientId) {
        return ResponseEntity.ok(referralService.getReferralInfo(clientId));
    }

    /**
     * POST /api/referral/apply?clientId=2&code=SHOP1234
     * Apply a referral code for a new client
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyCode(
            @RequestParam Long clientId,
            @RequestParam String code) {
        boolean applied = referralService.applyReferralCode(clientId, code);
        if (applied) {
            return ResponseEntity.ok(Map.of("message", "Referral code applied successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or already used referral code"));
        }
    }
}
