package com.sellerpro.controller;

import com.sellerpro.model.ActivityLog;
import com.sellerpro.model.FailedPayment;
import com.sellerpro.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminStatsService adminStatsService;
    private final ActivityLogService activityLogService;
    private final FailedPaymentRepository failedPaymentRepository;
    private final UserService userService;

    // ── Dashboard Stats ──────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(adminStatsService.getDashboardStats());
    }

    // ── Customer Management ──────────────────────────────────────────────────

    @GetMapping("/customers")
    public ResponseEntity<?> getCustomers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.searchCustomers(search, page, size));
    }

    @GetMapping("/customers/{email}")
    public ResponseEntity<?> getCustomerDetail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getCustomerDetail(email));
    }

    @DeleteMapping("/customers/{email}")
    public ResponseEntity<Void> removeCustomer(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }

    // ── Activity Logs ────────────────────────────────────────────────────────

    @GetMapping("/logs")
    public ResponseEntity<Page<ActivityLog>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String action) {
        if (action != null) {
            return ResponseEntity.ok(activityLogService.getLogsByAction(action, page, size));
        }
        return ResponseEntity.ok(activityLogService.getAllLogs(page, size));
    }

    @GetMapping("/logs/summary")
    public ResponseEntity<List<Map<String, Object>>> getLogsSummary() {
        return ResponseEntity.ok(activityLogService.getActionSummary());
    }

    @GetMapping("/logs/user/{email}")
    public ResponseEntity<Page<ActivityLog>> getUserLogs(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(activityLogService.getUserLogs(email, page, size));
    }

    // ── Failed Payments ──────────────────────────────────────────────────────

    @GetMapping("/failed-payments")
    public ResponseEntity<Page<FailedPayment>> getFailedPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
            failedPaymentRepository.findAllByOrderByAttemptedAtDesc(
                org.springframework.data.domain.PageRequest.of(page, size)));
    }

    @PatchMapping("/failed-payments/{id}/resolve")
    public ResponseEntity<FailedPayment> resolvePayment(@PathVariable Long id) {
        FailedPayment fp = failedPaymentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        fp.setStatus("RESOLVED");
        fp.setResolvedAt(java.time.LocalDateTime.now());
        return ResponseEntity.ok(failedPaymentRepository.save(fp));
    }
}
