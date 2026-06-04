package com.sellerpro.service;

import com.sellerpro.entity.Plan;
import com.sellerpro.entity.Subscription;
import com.sellerpro.entity.User;
import com.sellerpro.exception.ResourceNotFoundException;
import com.sellerpro.repository.PlanRepository;
import com.sellerpro.repository.SubscriptionRepository;
import com.sellerpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    /**
     * Get the currently active subscription for a user.
     */
    public Optional<Subscription> getActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveByUserId(userId, LocalDate.now());
    }

    /**
     * Get the active Plan for a user (convenience method).
     */
    public Optional<Plan> getActivePlan(Long userId) {
        return getActiveSubscription(userId).map(Subscription::getPlan);
    }

    /**
     * Check if a user can access a specific platform.
     */
    public boolean canAccessPlatform(Long userId, String platform) {
        return getActivePlan(userId)
            .map(plan -> plan.getAllowedPlatformList().contains(platform.toUpperCase()))
            .orElse(false);
    }

    /**
     * Check if a user has analytics access.
     */
    public boolean hasAnalyticsAccess(Long userId) {
        return getActivePlan(userId).map(Plan::isAnalyticsAccess).orElse(false);
    }

    /**
     * Assign a plan to a user — called by Admin or after payment success.
     */
    @Transactional
    public Subscription assignPlan(Long userId, Long planId, Long adminId, String notes) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        // Expire any existing active subscriptions
        expireExistingSubscriptions(userId);

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(plan.getDurationMonths());

        Subscription subscription = Subscription.builder()
            .userId(userId)
            .plan(plan)
            .status(Subscription.Status.ACTIVE)
            .startDate(start)
            .endDate(end)
            .createdById(adminId)
            .notes(notes)
            .build();

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Plan {} assigned to user {} until {}", plan.getName(), userId, end);
        return saved;
    }

    /**
     * Extend subscription by N months.
     */
    @Transactional
    public Subscription extendSubscription(Long userId, int months, Long adminId) {
        Subscription subscription = getActiveSubscription(userId)
            .orElseThrow(() -> new ResourceNotFoundException("No active subscription for user " + userId));

        subscription.setEndDate(subscription.getEndDate().plusMonths(months));
        subscription.setCreatedById(adminId);
        return subscriptionRepository.save(subscription);
    }

    /**
     * Cancel subscription immediately.
     */
    @Transactional
    public void cancelSubscription(Long userId) {
        getActiveSubscription(userId).ifPresent(sub -> {
            sub.setStatus(Subscription.Status.CANCELLED);
            subscriptionRepository.save(sub);
        });
    }

    /**
     * Give trial (7 days BASIC) to a new user.
     */
    @Transactional
    public Subscription giveFreeTrial(Long userId) {
        Plan basicPlan = planRepository.findByName(Plan.PlanName.BASIC)
            .orElseThrow(() -> new ResourceNotFoundException("Basic plan not found"));

        Subscription trial = Subscription.builder()
            .userId(userId)
            .plan(basicPlan)
            .status(Subscription.Status.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(7))
            .trial(true)
            .notes("7-day free trial")
            .build();

        return subscriptionRepository.save(trial);
    }

    /**
     * Scheduled: expire subscriptions that are past end date.
     */
    @Scheduled(cron = "0 0 1 * * *") // 1 AM daily
    @Transactional
    public void expireOldSubscriptions() {
        log.info("Running subscription expiry job...");
        List<Subscription> allActive = subscriptionRepository.findAll().stream()
            .filter(s -> s.getStatus() == Subscription.Status.ACTIVE && s.isExpired())
            .toList();

        allActive.forEach(s -> {
            s.setStatus(Subscription.Status.EXPIRED);
            subscriptionRepository.save(s);
            log.info("Expired subscription {} for user {}", s.getId(), s.getUserId());
        });
    }

    private void expireExistingSubscriptions(Long userId) {
        subscriptionRepository.findByUserId(userId).stream()
            .filter(s -> s.getStatus() == Subscription.Status.ACTIVE)
            .forEach(s -> {
                s.setStatus(Subscription.Status.EXPIRED);
                subscriptionRepository.save(s);
            });
    }
}
