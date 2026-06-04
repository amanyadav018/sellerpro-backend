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
 * Parses Meesho Supplier Hub payment reports (.csv).
 */
@Component
public class MeeshoParser extends BaseParser {

    @Override
    public String getPlatform() {
        return "MEESHO";
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
                "sub order no", "order id", "suborderid", "sub_order_no");
        if (orderId == null || orderId.isBlank()) return null;

        String dateStr = m.getValue(row,
                "order date", "date", "created at", "order_date");
        LocalDate orderDate = parseDate(dateStr);
        if (orderDate == null) orderDate = LocalDate.now();

        // Meesho uses "order amount" as customer-facing price
        BigDecimal sellingPrice = parseDecimal(m.getValue(row,
                "order amount", "customer price", "selling price"));
        // Meesho charges no commission — their model is different
        BigDecimal marketplaceFee = parseDecimal(m.getValue(row,
                "meesho platform fees", "platform fees", "commission"));
        BigDecimal shippingFee = parseDecimal(m.getValue(row,
                "logistics charges", "shipping fee"));
        BigDecimal tcsAmount = parseDecimal(m.getValue(row,
                "tcs", "tax collected"));
        BigDecimal settlement = parseDecimal(m.getValue(row,
                "supplier payment amount", "net payment", "settlement amount",
                "amount to be paid to supplier"));

        BigDecimal netRevenue = settlement.compareTo(BigDecimal.ZERO) > 0
                ? settlement
                : sellingPrice.subtract(marketplaceFee.abs()).subtract(shippingFee.abs());

        String sku = m.getValue(row, "sku", "seller sku", "supplier sku");
        String productName = m.getValue(row, "product name", "item name", "name");
        String qty = m.getValue(row, "quantity", "qty", "units");
        String status = m.getValue(row, "order status", "status");
        String returnStatus = m.getValue(row, "return type", "return status");
        String returnReason = m.getValue(row, "return reason", "cancellation reason");
        String city = m.getValue(row, "city", "customer city");
        String state = m.getValue(row, "state", "customer state");
        String pincode = m.getValue(row, "pincode", "postal code");
        String paymentMode = m.getValue(row, "payment mode", "payment type");

        Map<String, Object> rawData = new HashMap<>();
        rawData.put("source", "meesho_payment_report");
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
                .paymentMode(paymentMode)
                .batchId(batchId)
                .rawData(rawData)
                .build();
    }
}
