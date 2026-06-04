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
 * Parses custom website order exports — WooCommerce, Shopify, Magento, etc.
 * Uses flexible column matching to support various export formats.
 */
@Component
public class WebsiteParser extends BaseParser {

    @Override
    public String getPlatform() {
        return "WEBSITE";
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
                "order id", "order number", "id", "order#", "order_id");
        if (orderId == null || orderId.isBlank()) return null;

        String dateStr = m.getValue(row,
                "order date", "date", "created at", "created_at", "order_date");
        LocalDate orderDate = parseDate(dateStr);
        if (orderDate == null) orderDate = LocalDate.now();

        BigDecimal sellingPrice = parseDecimal(m.getValue(row,
                "total", "order total", "amount", "price", "selling price", "subtotal"));
        BigDecimal shippingFee = parseDecimal(m.getValue(row,
                "shipping", "shipping amount", "delivery charges"));
        BigDecimal netRevenue = sellingPrice.subtract(shippingFee.abs());

        String sku = m.getValue(row, "sku", "product sku", "item sku", "variant sku");
        String productName = m.getValue(row,
                "product", "product name", "item", "name", "lineitem name");
        String qty = m.getValue(row, "quantity", "qty", "lineitem quantity");
        String status = m.getValue(row, "status", "order status", "fulfillment status");
        String city = m.getValue(row,
                "shipping city", "city", "billing city", "ship to city");
        String state = m.getValue(row,
                "shipping province", "state", "billing state", "ship to state");
        String pincode = m.getValue(row,
                "shipping zip", "pincode", "billing zip", "postal code");
        String paymentMode = m.getValue(row,
                "payment method", "payment mode", "payment gateway");

        Map<String, Object> rawData = new HashMap<>();
        rawData.put("source", "website_export");
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
                .shippingFee(shippingFee.abs())
                .netRevenue(netRevenue)
                .orderStatus(status)
                .buyerCity(city)
                .buyerState(state)
                .buyerPincode(pincode)
                .paymentMode(paymentMode)
                .batchId(batchId)
                .rawData(rawData)
                .build();
    }
}
