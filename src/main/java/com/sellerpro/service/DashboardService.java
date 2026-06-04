package com.sellerpro.service;

import com.sellerpro.dto.DashboardResponse;
import com.sellerpro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;

    public DashboardResponse getDashboard(Long clientId, LocalDate from, LocalDate to) {

        // Revenue
        BigDecimal totalRevenue = orderRepository.sumNetRevenue(clientId, from, to);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // Counts
        long totalOrders = orderRepository.findByClientIdAndOrderDateBetween(clientId, from, to).size();
        long totalReturns = orderRepository.countReturns(clientId);

        double returnRate = totalOrders > 0
                ? (double) totalReturns / totalOrders * 100
                : 0.0;

        // Platform breakdown
        List<Object[]> rawBreakdown = orderRepository.getPlatformBreakdown(clientId, from, to);
        List<DashboardResponse.PlatformStat> platformStats = new ArrayList<>();
        for (Object[] row : rawBreakdown) {
            platformStats.add(DashboardResponse.PlatformStat.builder()
                    .platform((String) row[0])
                    .orderCount(((Number) row[1]).longValue())
                    .revenue(row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO)
                    .build());
        }

        // Top SKUs
        List<Object[]> rawSkus = orderRepository.getSkuRanking(clientId, from, to);
        List<DashboardResponse.SkuStat> topSkus = new ArrayList<>();
        int limit = Math.min(10, rawSkus.size());
        for (int i = 0; i < limit; i++) {
            Object[] row = rawSkus.get(i);
            topSkus.add(DashboardResponse.SkuStat.builder()
                    .sku((String) row[0])
                    .productName((String) row[1])
                    .totalQuantity(((Number) row[2]).longValue())
                    .totalRevenue(row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO)
                    .build());
        }

        // State-wise breakdown
        List<Object[]> rawStates = orderRepository.getOrdersByState(clientId);
        List<DashboardResponse.StateStat> stateStats = new ArrayList<>();
        int stateLimit = Math.min(10, rawStates.size());
        for (int i = 0; i < stateLimit; i++) {
            Object[] row = rawStates.get(i);
            stateStats.add(DashboardResponse.StateStat.builder()
                    .state(row[0] != null ? (String) row[0] : "Unknown")
                    .orderCount(((Number) row[1]).longValue())
                    .build());
        }

        return DashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalReturns(totalReturns)
                .returnRate(Math.round(returnRate * 100.0) / 100.0)
                .platformStats(platformStats)
                .topSkus(topSkus)
                .stateStats(stateStats)
                .fromDate(from)
                .toDate(to)
                .build();
    }
}
