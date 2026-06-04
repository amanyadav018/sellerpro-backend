package com.sellerpro.parser;

import com.sellerpro.entity.Client;
import com.sellerpro.entity.Order;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses Flipkart Seller Hub payment/order reports (.csv).
 */
@Component
public class FlipkartParser extends BaseParser {

    @Override
    public String getPlatform() {
        return "FLIPKART";
    }

    @Override
    public ParseResult parse(MultipartFile file, Client client) throws Exception {
        String batchId = generateBatchId();
        ParseResult result = ParseResult.builder()
                .platform(getPlatform())
                .fileName(file.getOriginalFilename())
                .batchId(batchId)
                .build();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) {
                result.addError("File is empty");
                return result;
            }

            // Find header row (skip initial summary rows Flipkart sometimes adds)
            int headerIdx = findHeaderRow(allRows);
            if (headerIdx < 0) {
                result.addError("Could not find header row");
                return result;
            }

            ColumnMapper mapper = new ColumnMapper(allRows.get(headerIdx));
            result.setTotalRows(allRows.size() - headerIdx - 1);

            for (int i = headerIdx + 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                if (isSkipRow(row)) continue;

                try {
                    Order order = buildOrder(row, mapper, client, batchId);
                    if (order != null) result.addOrder(order);
                    else result.setSkipCount(result.getSkipCount() + 1);
                } catch (Exception e) {
                    result.addError("Row " + i + ": " + e.getMessage());
                }
            }
        }

        return result;
    }

    private int findHeaderRow(List<String[]> rows) {
        for (int i = 0; i < Math.min(10, rows.size()); i++) {
            String[] row = rows.get(i);
            for (String cell : row) {
                if (cell != null && (cell.toLowerCase().contains("order id")
                        || cell.toLowerCase().contains("order id")
                        || cell.toLowerCase().contains("fsn"))) {
                    return i;
                }
            }
        }
        return 0; // default to first row
    }

    private Order buildOrder(String[] row, ColumnMapper m, Client client, String batchId) {
        String orderId = m.getValue(row,
                "order id", "order item id", "flipkart order id");
        if (orderId == null || orderId.isBlank()) return null;

        String dateStr = m.getValue(row,
                "order date", "date", "order creation date");
        LocalDate orderDate = parseDate(dateStr);
        if (orderDate == null) orderDate = LocalDate.now();

        BigDecimal sellingPrice = parseDecimal(m.getValue(row,
                "selling price", "customer paid price", "final amount"));
        BigDecimal marketplaceFee = parseDecimal(m.getValue(row,
                "commission", "marketplace fees", "platform fees"));
        BigDecimal shippingFee = parseDecimal(m.getValue(row,
                "shipping charges", "logistics charges", "courier charges"));
        BigDecimal tcsAmount = parseDecimal(m.getValue(row,
                "tcs", "tcs amount", "tcs-igst"));
        BigDecimal settlement = parseDecimal(m.getValue(row,
                "settlement amount", "net settlement", "net payment"));

        BigDecimal netRevenue = sellingPrice
                .subtract(marketplaceFee.abs())
                .subtract(shippingFee.abs());

        String sku = m.getValue(row, "seller sku", "sku", "item sku");
        String productName = m.getValue(row, "product title", "product name", "item");
        String qty = m.getValue(row, "quantity", "qty");
        String status = m.getValue(row, "order status", "status");
        String returnStatus = m.getValue(row, "return status", "return type");
        String returnReason = m.getValue(row, "return reason", "cancellation reason");
        String city = m.getValue(row, "city", "customer city", "delivery city");
        String state = m.getValue(row, "state", "customer state", "delivery state");
        String pincode = m.getValue(row, "pincode", "postal code", "delivery pincode");

        Map<String, Object> rawData = new HashMap<>();
        rawData.put("source", "flipkart_payment_report");
        rawData.put("batchId", batchId);

        return Order.builder()
                .client(client)
                .platform(getPlatform())
                .orderId(orderId)
                .orderDate(orderDate)
                .sku(sku)
                .productName(productName)
                .quantity(parseInteger(qty))
                .sellingPrice(sellingPrice)
                .marketplaceFee(marketplaceFee.abs())
                .shippingFee(shippingFee.abs())
                .tcsAmount(tcsAmount)
                .settlementAmount(settlement)
                .netRevenue(netRevenue)
                .orderStatus(status)
                .returnStatus(returnStatus)
                .returnReason(returnReason)
                .buyerCity(city)
                .buyerState(state)
                .buyerPincode(pincode)
                .batchId(batchId)
                .rawData(rawData)
                .build();
    }
}
