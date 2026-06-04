package com.sellerpro.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private BigDecimal totalRevenue;
    private long totalOrders;
    private long totalReturns;
    private double returnRate;
    private LocalDate fromDate;
    private LocalDate toDate;

    private List<PlatformStat> platformStats;
    private List<SkuStat> topSkus;
    private List<StateStat> stateStats;

    @Data
    @Builder
    public static class PlatformStat {
        private String platform;
        private long orderCount;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    public static class SkuStat {
        private String sku;
        private String productName;
        private long totalQuantity;
        private BigDecimal totalRevenue;
    }

    @Data
    @Builder
    public static class StateStat {
        private String state;
        private long orderCount;
    }
}
