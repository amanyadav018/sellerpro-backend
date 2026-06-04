package com.sellerpro.service;

import com.sellerpro.model.Plan;
import com.sellerpro.model.Subscription;
import com.sellerpro.repository.PlanRepository;
import com.sellerpro.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    public Optional<Subscription> getActiveSubscription(String email) {
        return subscriptionRepository
            .findTopByUserEmailAndStatusOrderByCreatedAtDesc(email, "ACTIVE");
    }

    public String getUserPlan(String email) {
        return getActiveSubscription(email)
            .map(s -> s.getPlan().getName())
            .orElse("BASIC");
    }

    public Subscription assignPlan(String email, String planName, int months) {
        // Expire existing active subscription
        getActiveSubscription(email).ifPresent(s -> {
            s.setStatus("CANCELLED");
            subscriptionRepository.save(s);
        });

        Plan plan = planRepository.findByName(planName.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Plan not found"));

        Subscription sub = Subscription.builder()
            .userEmail(email)
            .plan(plan)
            .status("ACTIVE")
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(months))
            .build();

        return subscriptionRepository.save(sub);
    }

    public Subscription extendSubscription(String email, int months) {
        Subscription sub = getActiveSubscription(email)
            .orElseThrow(() -> new RuntimeException("No active subscription"));

        sub.setEndDate(sub.getEndDate().plusMonths(months));
        sub.setBonusMonthsApplied(sub.getBonusMonthsApplied() + months);
        return subscriptionRepository.save(sub);
    }

    public void suspendSubscription(String email) {
        getActiveSubscription(email).ifPresent(s -> {
            s.setStatus("SUSPENDED");
            subscriptionRepository.save(s);
        });
    }

    public void cancelSubscription(String email) {
        getActiveSubscription(email).ifPresent(s -> {
            s.setStatus("CANCELLED");
            subscriptionRepository.save(s);
        });
    }

    public List<Map<String, Object>> getAllSubscriptionsForAdmin() {
        return subscriptionRepository.findAllWithUserDetails().stream().map(row -> Map.of(
            "email", row[0], "name", row[1], "plan", row[2],
            "startDate", row[3], "endDate", row[4], "status", row[5]
        )).toList();
    }

    // Daily job to expire subscriptions
    @Scheduled(cron = "0 0 1 * * *") // 1 AM every day
    public void expireOldSubscriptions() {
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(LocalDate.now());
        expired.forEach(s -> {
            s.setStatus("EXPIRED");
            subscriptionRepository.save(s);
        });
    }
}
