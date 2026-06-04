package com.sellerpro.controller;

import com.sellerpro.dto.GstReportResponse;
import com.sellerpro.service.GstReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/gst")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GstReportController {

    private final GstReportService gstReportService;

    /**
     * GET /api/gst/report?clientId=1&period=2024-01
     * Returns full GST report for the given return period
     */
    @GetMapping("/report")
    public ResponseEntity<GstReportResponse> getReport(
            @RequestParam Long clientId,
            @RequestParam(required = false) String period) {

        // Default to current month if period not provided
        if (period == null || period.isBlank()) {
            period = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        GstReportResponse report = gstReportService.generateReport(clientId, period);
        return ResponseEntity.ok(report);
    }

    /**
     * POST /api/gst/compute?clientId=1&period=2024-01
     * Force recompute GST entries from existing orders for a period
     */
    @PostMapping("/compute")
    public ResponseEntity<?> recompute(
            @RequestParam Long clientId,
            @RequestParam String period) {

        gstReportService.computeAndSaveFromOrders(clientId, period);
        GstReportResponse report = gstReportService.generateReport(clientId, period);
        return ResponseEntity.ok(report);
    }
}
