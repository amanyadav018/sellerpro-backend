package com.sellerpro.service;

import com.sellerpro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthComparisonService {

    private final OrderRepository orderRepository;

    public List<Map<String, Object>> getMonthlyTrend(String email, int months) {
        LocalDate from = LocalDate.now().minusMonths(months);
        List<Object[]> data = orderRepository.findMonthlyTrend(email, from);

        return data.stream().map(row -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("month", row[0]);
            entry.put("year", row[1]);
            entry.put("orderCount", row[2]);
            entry.put("revenue", row[3]);
            entry.put("returnCount", row[4]);
            return entry;
        }).collect(Collectors.toList());
    }

    public double getGrowthPercent(String email) {
        List<Object[]> last2Months = orderRepository.findMonthlyTrend(
            email, LocalDate.now().minusMonths(2));

        if (last2Months == null || last2Months.size() < 2) return 0.0;

        double prevRevenue = ((Number) last2Months.get(0)[3]).doubleValue();
        double currRevenue = ((Number) last2Months.get(1)[3]).doubleValue();

        if (prevRevenue == 0) return 0.0;
        return Math.round(((currRevenue - prevRevenue) / prevRevenue) * 10000.0) / 100.0;
    }
}
