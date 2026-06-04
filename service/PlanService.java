package com.sellerpro.service;

import com.sellerpro.model.Plan;
import com.sellerpro.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    public List<Plan> getAllActivePlans() {
        return planRepository.findAllActiveSorted();
    }

    public Plan getPlanByName(String name) {
        return planRepository.findByName(name.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Plan not found: " + name));
    }

    public Plan createPlan(Plan plan) {
        plan.setName(plan.getName().toUpperCase());
        return planRepository.save(plan);
    }

    public Plan updatePlan(Long id, Plan updated) {
        Plan existing = planRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Plan not found"));

        existing.setDisplayName(updated.getDisplayName());
        existing.setPrice(updated.getPrice());
        existing.setDescription(updated.getDescription());
        existing.setAllowedPlatforms(updated.getAllowedPlatforms());
        existing.setMaxMonthsHistory(updated.getMaxMonthsHistory());
        existing.setAnalyticsAccess(updated.isAnalyticsAccess());
        existing.setWhatsappDigest(updated.isWhatsappDigest());
        existing.setSmartAlerts(updated.isSmartAlerts());
        existing.setGstReportV2(updated.isGstReportV2());
        existing.setReferralBonus(updated.isReferralBonus());

        return planRepository.save(existing);
    }

    public Plan toggleFeature(Long id, String feature, boolean enabled) {
        Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Plan not found"));

        switch (feature.toLowerCase()) {
            case "analytics" -> plan.setAnalyticsAccess(enabled);
            case "whatsapp" -> plan.setWhatsappDigest(enabled);
            case "alerts" -> plan.setSmartAlerts(enabled);
            case "gst" -> plan.setGstReportV2(enabled);
            case "referral" -> plan.setReferralBonus(enabled);
            default -> throw new RuntimeException("Unknown feature: " + feature);
        }

        return planRepository.save(plan);
    }

    public void deactivatePlan(Long id) {
        Plan plan = planRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Plan not found"));
        plan.setActive(false);
        planRepository.save(plan);
    }

    public Map<String, Object> getPlanFeatures(String planName) {
        Plan plan = getPlanByName(planName);
        return Map.of(
            "allowedPlatforms", plan.getAllowedPlatforms().split(","),
            "maxMonthsHistory", plan.getMaxMonthsHistory(),
            "analyticsAccess", plan.isAnalyticsAccess(),
            "whatsappDigest", plan.isWhatsappDigest(),
            "smartAlerts", plan.isSmartAlerts(),
            "gstReportV2", plan.isGstReportV2()
        );
    }
}
