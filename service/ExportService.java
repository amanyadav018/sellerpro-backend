package com.sellerpro.service;

import com.sellerpro.dto.ExportFilter;
import com.sellerpro.entity.Order;
import com.sellerpro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final OrderRepository orderRepository;

    /**
     * Export orders to CSV bytes
     */
    public byte[] exportToCsv(ExportFilter filter) throws IOException {
        List<Order> orders = fetchOrders(filter);
        log.info("Exporting {} orders to CSV for client {}", orders.size(), filter.getClientId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);

        // BOM for Excel UTF-8 compatibility
        writer.write('\uFEFF');

        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                "Order ID", "Platform", "Order Date", "Customer Name",
                "Product", "SKU", "Quantity", "Sale Amount",
                "Selling Price", "Commission", "Net Payout",
                "Status", "Shipping State", "Batch ID"))) {

            for (Order o : orders) {
                printer.printRecord(
                    o.getOrderId(),
                    o.getPlatform(),
                    o.getOrderDate() != null ? o.getOrderDate().toString() : "",
                    o.getCustomerName(),
                    o.getProductName(),
                    o.getSku(),
                    o.getQuantity(),
                    o.getSaleAmount(),
                    o.getSellingPrice(),
                    o.getCommission(),
                    o.getNetPayout(),
                    o.getStatus(),
                    o.getShippingState(),
                    o.getBatchId()
                );
            }
        }

        return out.toByteArray();
    }

    /**
     * Export orders to Excel (.xlsx) bytes
     */
    public byte[] exportToExcel(ExportFilter filter) throws IOException {
        List<Order> orders = fetchOrders(filter);
        log.info("Exporting {} orders to Excel for client {}", orders.size(), filter.getClientId());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Orders");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            String[] headers = {"Order ID", "Platform", "Order Date", "Customer Name",
                "Product", "SKU", "Qty", "Sale Amount", "Selling Price",
                "Commission", "Net Payout", "Status", "State", "Batch ID"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (Order o : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(o.getOrderId() != null ? o.getOrderId() : "");
                row.createCell(1).setCellValue(o.getPlatform() != null ? o.getPlatform() : "");
                row.createCell(2).setCellValue(o.getOrderDate() != null ? o.getOrderDate().toString() : "");
                row.createCell(3).setCellValue(o.getCustomerName() != null ? o.getCustomerName() : "");
                row.createCell(4).setCellValue(o.getProductName() != null ? o.getProductName() : "");
                row.createCell(5).setCellValue(o.getSku() != null ? o.getSku() : "");
                row.createCell(6).setCellValue(o.getQuantity() != null ? o.getQuantity() : 0);
                if (o.getSaleAmount() != null) row.createCell(7).setCellValue(o.getSaleAmount().doubleValue());
                if (o.getSellingPrice() != null) row.createCell(8).setCellValue(o.getSellingPrice().doubleValue());
                if (o.getCommission() != null) row.createCell(9).setCellValue(o.getCommission().doubleValue());
                if (o.getNetPayout() != null) row.createCell(10).setCellValue(o.getNetPayout().doubleValue());
                row.createCell(11).setCellValue(o.getStatus() != null ? o.getStatus() : "");
                row.createCell(12).setCellValue(o.getShippingState() != null ? o.getShippingState() : "");
                row.createCell(13).setCellValue(o.getBatchId() != null ? o.getBatchId().toString() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private List<Order> fetchOrders(ExportFilter filter) {
        LocalDate start = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate end = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();

        if (filter.getPlatform() != null && !filter.getPlatform().isBlank()) {
            return orderRepository.findByClientIdAndPlatformAndOrderDateBetween(
                filter.getClientId(), filter.getPlatform(), start, end);
        }
        return orderRepository.findByClientIdAndOrderDateBetween(filter.getClientId(), start, end);
    }
}
