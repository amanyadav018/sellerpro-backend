package com.sellerpro.controller;

import com.sellerpro.dto.DashboardResponse;
import com.sellerpro.entity.Client;
import com.sellerpro.entity.User;
import com.sellerpro.repository.ClientRepository;
import com.sellerpro.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final ClientRepository clientRepository;

    /**
     * GET /api/dashboard?from=2024-01-01&to=2024-03-31
     * Returns full dashboard: revenue, orders, platform breakdown, top SKUs, state breakdown.
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // Default: last 3 months
        if (from == null) from = LocalDate.now().minusMonths(3);
        if (to == null) to = LocalDate.now();

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        DashboardResponse response = dashboardService.getDashboard(client.getId(), from, to);
        return ResponseEntity.ok(response);
    }
}
