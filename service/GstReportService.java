package com.sellerpro.service;

import com.sellerpro.dto.GstReportResponse;
import com.sellerpro.entity.GstEntry;
import com.sellerpro.entity.Order;
import com.sellerpro.repository.GstEntryRepository;
import com.sellerpro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GstReportService {

    private final GstEntryRepository gstEntryRepository;
    private final OrderRepository orderRepository;

    // Seller's home state - should come from client config in production
    private static final String SELLER_STATE = "Maharashtra";

    // GST rates by HSN category
    private static final Map<String, BigDecimal> GST_RATES = Map.of(
        "APPAREL", new BigDecimal("12"),
        "ELECTRONICS", new BigDecimal("18"),
        "BEAUTY", new BigDecimal("18"),
        "GROCERY", new BigDecimal("5"),
        "DEFAULT", new BigDecimal("18")
    );

    /**
     * Generate GST report for a given return period (format: "2024-01")
     */
    @Transactional
    public GstReportResponse generateReport(Long clientId, String returnPeriod) {
        log.info("Generating GST report for client {} period {}", clientId, returnPeriod);

        // Fetch all GST entries for this period
        List<GstEntry> entries = gstEntryRepository
            .findByClientIdAndReturnPeriodOrderByOrderDateAsc(clientId, returnPeriod);

        if (entries.isEmpty()) {
            // Try to generate from orders if no pre-computed entries
            entries = computeAndSaveFromOrders(clientId, returnPeriod);
        }

        return buildResponse(clientId, returnPeriod, entries);
    }

    /**
     * Compute GST entries from existing orders and persist them
     */
    @Transactional
    public List<GstEntry> computeAndSaveFromOrders(Long clientId, String returnPeriod) {
        String[] parts = returnPeriod.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // Get orders in the period (assumes OrderRepository has this method)
        List<Order> orders = orderRepository.findByClientIdAndOrderDateBetween(
            clientId, startDate, endDate);

        List<GstEntry> entries = new ArrayList<>();
        for (Order order : orders) {
            if (!gstEntryRepository.existsByClientIdAndOrderId(clientId, order.getOrderId())) {
                GstEntry entry = computeGstEntry(clientId, order, returnPeriod);
                entries.add(entry);
            }
        }

        return gstEntryRepository.saveAll(entries);
    }

    private GstEntry computeGstEntry(Long clientId, Order order, String returnPeriod) {
        BigDecimal saleAmount = order.getSaleAmount() != null ? order.getSaleAmount() : BigDecimal.ZERO;
        BigDecimal gstRate = GST_RATES.getOrDefault("DEFAULT", new BigDecimal("18"));

        // Taxable = saleAmount / (1 + gstRate/100)
        BigDecimal divisor = BigDecimal.ONE.add(gstRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        BigDecimal taxable = saleAmount.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal totalGst = saleAmount.subtract(taxable);

        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;

        String buyerState = order.getShippingState() != null ? order.getShippingState() : "Unknown";

        if (buyerState.equalsIgnoreCase(SELLER_STATE)) {
            // Intra-state: CGST + SGST each = totalGst / 2
            cgst = totalGst.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            sgst = totalGst.subtract(cgst);
        } else {
            // Inter-state: IGST = totalGst
            igst = totalGst;
        }

        return GstEntry.builder()
            .clientId(clientId)
            .orderId(order.getOrderId())
            .platform(order.getPlatform())
            .orderDate(order.getOrderDate())
            .buyerState(buyerState)
            .taxableAmount(taxable)
            .cgst(cgst)
            .sgst(sgst)
            .igst(igst)
            .totalGst(totalGst)
            .gstRate(gstRate + "%")
            .hsnCode("6109") // default HSN - should come from product catalog
            .invoiceNumber(order.getOrderId())
            .returnPeriod(returnPeriod)
            .build();
    }

    private GstReportResponse buildResponse(Long clientId, String returnPeriod, List<GstEntry> entries) {
        BigDecimal totalTaxable = sum(entries, GstEntry::getTaxableAmount);
        BigDecimal totalCgst = sum(entries, GstEntry::getCgst);
        BigDecimal totalSgst = sum(entries, GstEntry::getSgst);
        BigDecimal totalIgst = sum(entries, GstEntry::getIgst);
        BigDecimal totalGst = sum(entries, GstEntry::getTotalGst);

        // Platform breakdown
        Map<String, List<GstEntry>> byPlatform = entries.stream()
            .collect(Collectors.groupingBy(e -> e.getPlatform() != null ? e.getPlatform() : "UNKNOWN"));

        List<GstReportResponse.PlatformGstSummary> platformBreakdown = byPlatform.entrySet().stream()
            .map(entry -> GstReportResponse.PlatformGstSummary.builder()
                .platform(entry.getKey())
                .orderCount(entry.getValue().size())
                .taxableAmount(sum(entry.getValue(), GstEntry::getTaxableAmount))
                .cgst(sum(entry.getValue(), GstEntry::getCgst))
                .sgst(sum(entry.getValue(), GstEntry::getSgst))
                .igst(sum(entry.getValue(), GstEntry::getIgst))
                .totalGst(sum(entry.getValue(), GstEntry::getTotalGst))
                .build())
            .sorted(Comparator.comparing(GstReportResponse.PlatformGstSummary::getTotalGst).reversed())
            .collect(Collectors.toList());

        // State breakdown
        Map<String, List<GstEntry>> byState = entries.stream()
            .collect(Collectors.groupingBy(e -> e.getBuyerState() != null ? e.getBuyerState() : "Unknown"));

        List<GstReportResponse.StateGstSummary> stateBreakdown = byState.entrySet().stream()
            .map(entry -> GstReportResponse.StateGstSummary.builder()
                .state(entry.getKey())
                .orderCount(entry.getValue().size())
                .taxableAmount(sum(entry.getValue(), GstEntry::getTaxableAmount))
                .cgst(sum(entry.getValue(), GstEntry::getCgst))
                .sgst(sum(entry.getValue(), GstEntry::getSgst))
                .igst(sum(entry.getValue(), GstEntry::getIgst))
                .totalGst(sum(entry.getValue(), GstEntry::getTotalGst))
                .build())
            .sorted(Comparator.comparing(GstReportResponse.StateGstSummary::getTotalGst).reversed())
            .collect(Collectors.toList());

        // Line items
        List<GstReportResponse.GstLineItem> lineItems = entries.stream()
            .map(e -> GstReportResponse.GstLineItem.builder()
                .orderId(e.getOrderId())
                .invoiceNumber(e.getInvoiceNumber())
                .orderDate(e.getOrderDate() != null ? e.getOrderDate().toString() : "")
                .platform(e.getPlatform())
                .buyerState(e.getBuyerState())
                .taxableAmount(e.getTaxableAmount())
                .gstRate(e.getGstRate())
                .hsnCode(e.getHsnCode())
                .cgst(e.getCgst())
                .sgst(e.getSgst())
                .igst(e.getIgst())
                .totalGst(e.getTotalGst())
                .build())
            .collect(Collectors.toList());

        // Period label e.g. "January 2024"
        String[] parts = returnPeriod.split("-");
        String periodLabel = LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 1)
            .format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        return GstReportResponse.builder()
            .returnPeriod(returnPeriod)
            .periodLabel(periodLabel)
            .clientId(clientId)
            .totalTaxableAmount(totalTaxable)
            .totalCgst(totalCgst)
            .totalSgst(totalSgst)
            .totalIgst(totalIgst)
            .totalGst(totalGst)
            .totalOrders(entries.size())
            .platformBreakdown(platformBreakdown)
            .stateBreakdown(stateBreakdown)
            .lineItems(lineItems)
            .build();
    }

    @FunctionalInterface
    interface Extractor {
        BigDecimal get(GstEntry entry);
    }

    private BigDecimal sum(List<GstEntry> entries, Extractor extractor) {
        return entries.stream()
            .map(extractor::get)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
