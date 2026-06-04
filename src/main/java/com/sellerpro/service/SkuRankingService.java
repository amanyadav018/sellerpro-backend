package com.sellerpro.service;

import com.sellerpro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkuRankingService {

    private final OrderRepository orderRepository;

    public List<Map<String, Object>> getTopSkus(String email, int limit, String platform) {
        List<Object[]> skuData = platform != null
            ? orderRepository.findTopSkusByPlatform(email, platform, limit)
            : orderRepository.findTopSkus(email, limit);

        return skuData.stream().map(row -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("sku", row[0]);
            entry.put("productName", row[1]);
            entry.put("platform", row[2]);
            entry.put("totalQuantity", row[3]);
            entry.put("totalRevenue", row[4]);
            entry.put("returnRate", row[5]);
            return entry;
        }).collect(Collectors.toList());
    }

    public long getTotalOrders(String email) {
        return orderRepository.countByUserEmail(email);
    }
}
