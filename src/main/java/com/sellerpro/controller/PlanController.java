package com.sellerpro.controller;

import com.sellerpro.entity.Plan;
import com.sellerpro.entity.Subscription;
import com.sellerpro.entity.User;
import com.sellerpro.exception.ResourceNotFoundException;
import com.sellerpro.repository.PlanRepository;
import com.sellerpro.service.ActivityLogService;
import com.sellerpro.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanRepository planRepository;
    private final SubscriptionService subscriptionService;
    private final ActivityLogService activityLogService;

    /** Public — no auth needed. Shows plans on Pricing page. */
    @GetMapping("/public")
    public ResponseEntity<List<Plan>> getPublicPlans() {
        List<Plan> plans = planRepository.findByActiveTrue().stream()
            .filter(p -> !"ADMIN".equals(p.getName()))
            .toList();
        return ResponseEntity.ok(plans);
    }

    /** Current user's active subscription info */
    @GetMapping("/my-plan")
    public ResponseEntity<?> getMyPlan(@AuthenticationPrincipal User user) {
        return subscriptionService.getActiveSubscription(user.getId())
            .map(sub -> ResponseEntity.ok(Map.of(
                "plan", sub.getPlan().getName(),
                "displayName", sub.getPlan().getDisplayName(),
                "price", sub.getPlan().getPrice(),
                "startDate", sub.getStartDate(),
                "endDate", sub.getEndDate(),
                "isTrial", sub.isTrial(),
                "status", sub.getStatus(),
                "features", Map.of(
                    "analyticsAccess", sub.getPlan().isAnalyticsAccess(),
                    "whatsappDigest", sub.getPlan().isWhatsappDigest(),
                    "gstReportV2", sub.getPlan().isGstReportV2(),
                    "smartAlerts", sub.getPlan().isSmartAlerts(),
                    "sellerScore", sub.getPlan().isSellerScore(),
                    "allowedPlatforms", sub.getPlan().getAllowedPlatformList(),
                    "maxMonthsHistory", sub.getPlan().getMaxMonthsHistory()
                )
            )))
            .orElse(ResponseEntity.ok(Map.of("plan", "NONE", "message", "No active subscription")));
    }

    // ---- ADMIN endpoints ----

    /** Admin: Assign plan to any user */
    @PostMapping("/admin/assign")
    public ResponseEntity<Subscription> adminAssignPlan(
            @AuthenticationPrincipal User admin,
            @RequestBody AssignPlanRequest req) {
        Subscription sub = subscriptionService.assignPlan(
            req.userId(), req.planId(), admin.getId(), req.notes()
        );
        activityLogService.log(admin.getId(), "PLAN_ASSIGN", "User", req.userId(),
            Map.of("planId", req.planId(), "userId", req.userId()), null);
        return ResponseEntity.ok(sub);
    }

    /** Admin: Extend subscription by months */
    @PostMapping("/admin/extend")
    public ResponseEntity<Subscription> adminExtend(
            @AuthenticationPrincipal User admin,
            @RequestBody ExtendRequest req) {
        Subscription sub = subscriptionService.extendSubscription(
            req.userId(), req.months(), admin.getId()
        );
        return ResponseEntity.ok(sub);
    }

    /** Admin: Cancel user subscription */
    @DeleteMapping("/admin/cancel/{userId}")
    public ResponseEntity<Void> adminCancel(@PathVariable Long userId,
                                             @AuthenticationPrincipal User admin) {
        subscriptionService.cancelSubscription(userId);
        activityLogService.log(admin.getId(), "PLAN_CANCEL", "User", userId, null, null);
        return ResponseEntity.ok().build();
    }

    /** Admin: Toggle plan feature */
    @PatchMapping("/admin/{planId}/toggle")
    public ResponseEntity<Plan> toggleFeature(
            @PathVariable Long planId,
            @RequestBody ToggleFeatureRequest req) {
        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        switch (req.feature()) {
            case "analyticsAccess" -> plan.setAnalyticsAccess(req.enabled());
            case "whatsappDigest"  -> plan.setWhatsappDigest(req.enabled());
            case "gstReportV2"     -> plan.setGstReportV2(req.enabled());
            case "smartAlerts"     -> plan.setSmartAlerts(req.enabled());
            case "sellerScore"     -> plan.setSellerScore(req.enabled());
            case "referralProgram" -> plan.setReferralProgram(req.enabled());
        }

        return ResponseEntity.ok(planRepository.save(plan));
    }

    /** Admin: Update plan price */
    @PatchMapping("/admin/{planId}/price")
    public ResponseEntity<Plan> updatePrice(@PathVariable Long planId,
                                             @RequestBody Map<String, BigDecimal> body) {
        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        plan.setPrice(body.get("price"));
        return ResponseEntity.ok(planRepository.save(plan));
    }

    // ---- Records ----

    public record AssignPlanRequest(Long userId, Long planId, String notes) {}
    public record ExtendRequest(Long userId, int months) {}
    public record ToggleFeatureRequest(String feature, boolean enabled) {}
}
