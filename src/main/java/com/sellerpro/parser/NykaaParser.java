package com.sellerpro.parser;

import com.sellerpro.entity.Client;
import com.sellerpro.entity.Order;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses Nykaa Seller NowHub settlement reports (.csv).
 */
@Component
public class NykaaParser extends BaseParser {

    @Override
    public String getPlatform() {
        return "NYKAA";
    }

    @Override
    public ParseResult parse(MultipartFile file, Client client) throws Exception {
        String batchId = generateBatchId();
        ParseResult result = ParseResult.builder()
                .platform(getPlatform())
                .fileName(file.getOriginalFilename())
                .batchId(batchId)
                .build();

        try (CSVReader reader = new CSVReader(new java.io.InputStreamReader(file.getInputStream()))) {
            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) {
                result.addError("File is empty");
                return result;
            }

            ColumnMapper mapper = new ColumnMapper(allRows.get(0));
            result.setTotalRows(allRows.size() - 1);

            for (int i = 1; i < allRows.size(); i++) {
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

    private Order buildOrder(String[] row, ColumnMapper m, Client client, String batchId) {
        String orderId = m.getValue(row,
                "order id", "nykaa order id", "order number");
        if (orderId == null || orderId.isBlank()) return null;

        String dateStr = m.getValue(row, "order date", "date");
        LocalDate orderDate = parseDate(dateStr);
        if (orderDate == null) orderDate = LocalDate.now();

        BigDecimal sellingPrice = parseDecimal(m.getValue(row,
                "selling price", "invoice value", "net sales"));
        BigDecimal marketplaceFee = parseDecimal(m.getValue(row,
                "commission", "platform fee", "nykaa commission"));
        BigDecimal shippingFee = parseDecimal(m.getValue(row,
                "shipping charges", "logistics charges"));
        BigDecimal tcsAmount = parseDecimal(m.getValue(row, "tcs"));
        BigDecimal settlement = parseDecimal(m.getValue(row,
                "net settlement", "amount payable", "settlement amount"));

        BigDecimal netRevenue = settlement.compareTo(BigDecimal.ZERO) > 0
                ? settlement
                : sellingPrice.subtract(marketplaceFee.abs()).subtract(shippingFee.abs());

        String sku = m.getValue(row, "sku", "product sku", "seller sku");
        String productName = m.getValue(row, "product name", "item name");
        String qty = m.getValue(row, "quantity", "qty");
        String status = m.getValue(row, "order status", "status");
        String returnStatus = m.getValue(row, "return status", "return type");
        String city = m.getValue(row, "city", "customer city");
        String state = m.getValue(row, "state", "customer state");

        Map<String, Object> rawData = new HashMap<>();
        rawData.put("source", "nykaa_settlement");
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
                .buyerCity(city)
                .buyerState(state)
                .batchId(batchId)
                .rawData(rawData)
                .build();
    }
}
