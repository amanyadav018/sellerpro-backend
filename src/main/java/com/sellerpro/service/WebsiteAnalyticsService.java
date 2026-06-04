package com.sellerpro.service;

import com.sellerpro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WebsiteAnalyticsService {

    private final OrderRepository orderRepository;

    public Map<String, Object> getWebsiteStats(String email) {
        Map<String, Object> stats = new LinkedHashMap<>();

        long websiteOrders = orderRepository.countByUserEmailAndPlatform(email, "WEBSITE");
        Object[] revenueData = (Object[]) orderRepository.findWebsiteRevenueSummary(email);

        stats.put("totalOrders", websiteOrders);
        stats.put("totalRevenue", revenueData != null ? revenueData[0] : 0);
        stats.put("avgOrderValue", revenueData != null ? revenueData[1] : 0);
        stats.put("topProducts", orderRepository.findTopSkusByPlatform(email, "WEBSITE", 5));
        stats.put("monthlyTrend", orderRepository.findMonthlyTrendByPlatform(email, "WEBSITE", 6));

        return stats;
    }
}
