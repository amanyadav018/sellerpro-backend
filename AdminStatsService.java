package com.sellerpro.service;

import com.sellerpro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final FailedPaymentRepository failedPaymentRepository;
    private final ReferralRepository referralRepository;
    private final ActivityLogRepository activityLogRepository;

    public Map<String, Object> getDashboardStats() {
        long totalCustomers = userRepository.count();
        long activeSubscriptions = subscriptionRepository.countActiveSubscriptions();
        Double monthlyRevenue = subscriptionRepository.sumMonthlyRevenue();
        long failedPayments = failedPaymentRepository.countByStatus("PENDING_RETRY");
        long totalReferrals = referralRepository.count();

        // Churn = customers with no active sub / total
        long inactiveCustomers = totalCustomers - activeSubscriptions;
        double churnRate = totalCustomers > 0
            ? Math.round((inactiveCustomers * 10000.0) / totalCustomers) / 100.0
            : 0.0;

        return Map.of(
            "totalCustomers", totalCustomers,
            "activeSubscriptions", activeSubscriptions,
            "monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : 0.0,
            "churnRate", churnRate,
            "failedPaymentsPending", failedPayments,
            "totalReferrals", totalReferrals
        );
    }
}
