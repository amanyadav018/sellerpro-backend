package com.sellerpro.controller;

import com.sellerpro.model.Plan;
import com.sellerpro.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    // Public — anyone can see plans for pricing page
    @GetMapping
    public ResponseEntity<List<Plan>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllActivePlans());
    }

    @GetMapping("/{name}/features")
    public ResponseEntity<Map<String, Object>> getPlanFeatures(@PathVariable String name) {
        return ResponseEntity.ok(planService.getPlanFeatures(name));
    }

    // Admin only below
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Plan> createPlan(@RequestBody Plan plan) {
        return ResponseEntity.ok(planService.createPlan(plan));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Plan> updatePlan(@PathVariable Long id, @RequestBody Plan plan) {
        return ResponseEntity.ok(planService.updatePlan(id, plan));
    }

    @PatchMapping("/{id}/feature")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Plan> toggleFeature(
            @PathVariable Long id,
            @RequestParam String feature,
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(planService.toggleFeature(id, feature, enabled));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivatePlan(@PathVariable Long id) {
        planService.deactivatePlan(id);
        return ResponseEntity.noContent().build();
    }
}
