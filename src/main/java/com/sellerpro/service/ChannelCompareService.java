
package com.sellerpro.service;
import java.time.LocalDate;
import java.util.List;
import com.sellerpro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelCompareService {

    private final OrderRepository orderRepository;

    public List<Map<String, Object>> getChannelComparison(String email, int months) {
        LocalDate from = LocalDate.now().minusMonths(months);
       List<Object[]> data = orderRepository.getPlatformBreakdown(1L, from, LocalDate.now());
        return data.stream().map(row -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("platform", row[0]);
            entry.put("orderCount", row[1]);
            entry.put("revenue", row[2]);
            entry.put("avgOrderValue", row[3]);
            entry.put("returnCount", row[4]);
            entry.put("returnRate", row[5]);
            return entry;
        }).collect(Collectors.toList());
    }

    public String getTopPlatform(String email) {
       List<Object[]> top = orderRepository.getPlatformBreakdown(1L, LocalDate.now().minusMonths(1), LocalDate.now());
if (top.isEmpty()) return "N/A";
return (String) top.get(0)[0];
    }
}
