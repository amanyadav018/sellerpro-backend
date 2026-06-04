package com.sellerpro.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GstReportResponse {

    private String returnPeriod;
    private String periodLabel; // "January 2024"
    private Long clientId;

    // Summary totals
    private BigDecimal totalTaxableAmount;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalIgst;
    private BigDecimal totalGst;
    private int totalOrders;

    // Platform-wise breakdown
    private List<PlatformGstSummary> platformBreakdown;

    // State-wise breakdown (for IGST vs CGST/SGST logic)
    private List<StateGstSummary> stateBreakdown;

    // Line items for GSTR-1
    private List<GstLineItem> lineItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlatformGstSummary {
        private String platform;
        private int orderCount;
        private BigDecimal taxableAmount;
        private BigDecimal cgst;
        private BigDecimal sgst;
        private BigDecimal igst;
        private BigDecimal totalGst;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StateGstSummary {
        private String state;
        private int orderCount;
        private BigDecimal taxableAmount;
        private BigDecimal cgst;
        private BigDecimal sgst;
        private BigDecimal igst;
        private BigDecimal totalGst;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GstLineItem {
        private String orderId;
        private String invoiceNumber;
        private String orderDate;
        private String platform;
        private String buyerState;
        private BigDecimal taxableAmount;
        private String gstRate;
        private String hsnCode;
        private BigDecimal cgst;
        private BigDecimal sgst;
        private BigDecimal igst;
        private BigDecimal totalGst;
    }
}
