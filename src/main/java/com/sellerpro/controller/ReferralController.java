package com.sellerpro.controller;

import com.sellerpro.model.Referral;
import com.sellerpro.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/referral")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyReferralInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(referralService.getReferralInfo(userDetails.getUsername()));
    }

    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyReferralCode(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String code = body.get("code");
        return ResponseEntity.ok(referralService.applyCode(userDetails.getUsername(), code));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        return ResponseEntity.ok(referralService.getLeaderboard());
    }
}
