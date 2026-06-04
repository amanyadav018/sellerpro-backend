package com.sellerpro.controller;

import com.sellerpro.dto.AlertResponse;
import com.sellerpro.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertService alertService;

    /**
     * GET /api/alerts?clientId=1
     * Returns alert summary with recent alerts
     */
    @GetMapping
    public ResponseEntity<AlertResponse.AlertSummary> getAlerts(@RequestParam Long clientId) {
        return ResponseEntity.ok(alertService.getAlerts(clientId));
    }

    /**
     * POST /api/alerts/run?clientId=1
     * Manually trigger alert checks (normally run by scheduler)
     */
    @PostMapping("/run")
    public ResponseEntity<List<AlertResponse>> runChecks(@RequestParam Long clientId) {
        List<AlertResponse> newAlerts = alertService.runChecks(clientId);
        return ResponseEntity.ok(newAlerts);
    }

    /**
     * PUT /api/alerts/read-all?clientId=1
     * Mark all alerts as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@RequestParam Long clientId) {
        alertService.markAllRead(clientId);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/alerts/{id}/read?clientId=1
     * Mark single alert as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, @RequestParam Long clientId) {
        alertService.markRead(id, clientId);
        return ResponseEntity.ok().build();
    }
}
