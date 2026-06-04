package com.sellerpro.service;

import com.sellerpro.entity.Plan;
import com.sellerpro.entity.Subscription;
import com.sellerpro.entity.SubscriptionStatus;
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

    public Optional<Subscription> getActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveSubscription(userId, LocalDate.now());
    }

    public Optional<Plan> getActivePlan(Long userId) {
        return getActiveSubscription(userId).map(Subscription::getPlan);
    }

    public boolean canAccessPlatform(Long userId, String platform) {
        return getActivePlan(userId)
            .map(plan -> plan.getAllowedPlatforms().contains(platform.toUpperCase()))
            .orElse(false);
    }

    public boolean hasAnalyticsAccess(Long userId) {
        return getActivePlan(userId).map(Plan::isAnalyticsAccess).orElse(false);
    }

    @Transactional
    public Subscription assignPlan(Long userId, Long planId, Long adminId, String notes) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        expireExistingSubscriptions(userId);

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(plan.getDurationMonths());

        Subscription subscription = Subscription.builder()
            .user(user)
            .plan(plan)
            .status(SubscriptionStatus.ACTIVE)
            .startDate(start)
            .endDate(end)
            .notes(notes)
            .build();

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Plan {} assigned to user {} until {}", plan.getName(), userId, end);
        return saved;
    }

    @Transactional
    public Subscription extendSubscription(Long userId, int months, Long adminId) {
        Subscription subscription = getActiveSubscription(userId)
            .orElseThrow(() -> new ResourceNotFoundException("No active subscription for user " + userId));

        subscription.setEndDate(subscription.getEndDate().plusMonths(months));
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void cancelSubscription(Long userId) {
        getActiveSubscription(userId).ifPresent(sub -> {
            sub.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(sub);
        });
    }

    @Transactional
    public Subscription giveFreeTrial(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Plan basicPlan = planRepository.findByName(Plan.PlanName.BASIC)
            .orElseThrow(() -> new ResourceNotFoundException("Basic plan not found"));

        Subscription trial = Subscription.builder()
            .user(user)
            .plan(basicPlan)
            .status(SubscriptionStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(7))
            .notes("7-day free trial")
            .build();

        return subscriptionRepository.save(trial);
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void expireOldSubscriptions() {
        log.info("Running subscription expiry job...");
        List<Subscription> allActive = subscriptionRepository.findExpiredActiveSubscriptions(LocalDate.now());

        allActive.forEach(s -> {
            s.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(s);
            log.info("Expired subscription {} for user {}", s.getId(), s.getUser().getId());
        });
    }

    private void expireExistingSubscriptions(Long userId) {
        subscriptionRepository.findByUserId(userId).stream()
            .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
            .forEach(s -> {
                s.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(s);
            });
    }
}
