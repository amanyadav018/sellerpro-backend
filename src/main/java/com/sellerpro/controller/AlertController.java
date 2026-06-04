package com.sellerpro.controller;

import com.sellerpro.annotation.RequiresPlan;
import com.sellerpro.model.AlertRule;
import com.sellerpro.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @RequiresPlan("PRO")
    public ResponseEntity<List<AlertRule>> getAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(alertService.getUserAlerts(userDetails.getUsername()));
    }

    @PostMapping
    @RequiresPlan("PRO")
    public ResponseEntity<AlertRule> createAlert(
            @RequestBody AlertRule alertRule,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(alertService.createAlert(userDetails.getUsername(), alertRule));
    }

    @PutMapping("/{id}")
    @RequiresPlan("PRO")
    public ResponseEntity<AlertRule> updateAlert(
            @PathVariable Long id,
            @RequestBody AlertRule alertRule,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(alertService.updateAlert(userDetails.getUsername(), id, alertRule));
    }

    @DeleteMapping("/{id}")
    @RequiresPlan("PRO")
    public ResponseEntity<Void> deleteAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        alertService.deleteAlert(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/triggered")
    @RequiresPlan("PRO")
    public ResponseEntity<List<Map<String, Object>>> getTriggeredAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(alertService.getTriggeredAlerts(userDetails.getUsername()));
    }

    @PutMapping("/triggered/{id}/read")
    @RequiresPlan("PRO")
    public ResponseEntity<Void> markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        alertService.markAsRead(userDetails.getUsername(), id);
        return ResponseEntity.ok().build();
    }
}
