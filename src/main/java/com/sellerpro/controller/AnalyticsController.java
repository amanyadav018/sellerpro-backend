package com.sellerpro.controller;

import com.sellerpro.annotation.RequiresPlan;
import com.sellerpro.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final CityHeatmapService cityHeatmapService;
    private final SkuRankingService skuRankingService;
    private final ChannelCompareService channelCompareService;
    private final WebsiteAnalyticsService websiteAnalyticsService;
    private final MonthComparisonService monthComparisonService;

    @GetMapping("/city-heatmap")
    @RequiresPlan("PRO")
    public ResponseEntity<?> getCityHeatmap(
            @RequestParam(defaultValue = "3") int months,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cityHeatmapService.getCityData(userDetails.getUsername(), months));
    }

    @GetMapping("/sku-ranking")
    @RequiresPlan("PRO")
    public ResponseEntity<?> getSkuRanking(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String platform,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(skuRankingService.getTopSkus(userDetails.getUsername(), limit, platform));
    }

    @GetMapping("/channel-compare")
    @RequiresPlan("PRO")
    public ResponseEntity<?> getChannelComparison(
            @RequestParam(defaultValue = "3") int months,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(channelCompareService.getChannelComparison(userDetails.getUsername(), months));
    }

    @GetMapping("/website")
    @RequiresPlan("PRO")
    public ResponseEntity<?> getWebsiteAnalytics(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(websiteAnalyticsService.getWebsiteStats(userDetails.getUsername()));
    }

    @GetMapping("/month-comparison")
    @RequiresPlan("PRO")
    public ResponseEntity<?> getMonthComparison(
            @RequestParam(defaultValue = "6") int months,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(monthComparisonService.getMonthlyTrend(userDetails.getUsername(), months));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getAnalyticsSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> summary = Map.of(
            "totalOrders", skuRankingService.getTotalOrders(userDetails.getUsername()),
            "topPlatform", channelCompareService.getTopPlatform(userDetails.getUsername()),
            "growthPercent", monthComparisonService.getGrowthPercent(userDetails.getUsername())
        );
        return ResponseEntity.ok(summary);
    }
}
