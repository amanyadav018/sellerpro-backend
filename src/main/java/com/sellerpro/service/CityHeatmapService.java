package com.sellerpro.service;

import com.sellerpro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityHeatmapService {

    private final OrderRepository orderRepository;

    public List<Map<String, Object>> getCityData(String email, int months) {
        LocalDate from = LocalDate.now().minusMonths(months);

        // Fetch city-wise order aggregation from DB
        List<Object[]> cityData = orderRepository.findCityOrderStats(email, from);

        return cityData.stream().map(row -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("city", row[0]);
            entry.put("state", row[1]);
            entry.put("orderCount", row[2]);
            entry.put("revenue", row[3]);
            entry.put("avgOrderValue", row[4]);
            return entry;
        }).collect(Collectors.toList());
    }
}
