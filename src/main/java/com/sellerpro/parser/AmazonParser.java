package com.sellerpro.parser;

import com.sellerpro.entity.Client;
import com.sellerpro.entity.Order;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Parses Amazon Seller Central settlement reports (.xlsx).
 * Handles both V1 and V2 settlement report formats.
 */
@Component
public class AmazonParser extends BaseParser {

    @Override
    public String getPlatform() {
        return "AMAZON";
    }

    @Override
    public ParseResult parse(MultipartFile file, Client client) throws Exception {
        String batchId = generateBatchId();
        ParseResult result = ParseResult.builder()
                .platform(getPlatform())
                .fileName(file.getOriginalFilename())
                .batchId(batchId)
                .build();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Read header row
            if (!rows.hasNext()) {
                result.addError("File is empty");
                return result;
            }

            Row headerRow = rows.next();
            String[] headers = readRowAsStrings(headerRow);
            ColumnMapper mapper = new ColumnMapper(headers);
            result.setTotalRows(sheet.getLastRowNum());

            while (rows.hasNext()) {
                Row row = rows.next();
                String[] rowData = readRowAsStrings(row);
                if (isSkipRow(rowData)) continue;

                try {
                    Order order = buildOrder(rowData, mapper, client, batchId);
                    if (order != null) {
                        result.addOrder(order);
                    } else {
                        result.setSkipCount(result.getSkipCount() + 1);
                    }
                } catch (Exception e) {
                    result.addError("Row " + row.getRowNum() + ": " + e.getMessage());
                }
            }
        }

        return result;
    }

    private Order buildOrder(String[] row, ColumnMapper m, Client client, String batchId) {
        // Amazon settlement has "type" column — skip non-order rows
        String type = m.getValue(row, "type", "transaction type", "transaction-type");
        if (type != null && !type.equalsIgnoreCase("Order")) return null;

        String orderId = m.getValue(row,
                "order id", "order-id", "orderid", "amazon order id");
        if (orderId == null || orderId.isBlank()) return null;

        String dateStr = m.getValue(row,
                "date/time", "date", "settlement start date", "order date");
        LocalDate orderDate = parseDate(dateStr);
        if (orderDate == null) orderDate = LocalDate.now();

        BigDecimal sellingPrice = parseDecimal(m.getValue(row,
                "product sales", "selling price", "item price", "sale amount"));
        BigDecimal marketplaceFee = parseDecimal(m.getValue(row,
                "selling fees", "marketplace fees", "referral fee"));
        BigDecimal shippingFee = parseDecimal(m.getValue(row,
                "fba fees", "shipping fees", "fulfilment fees"));
        BigDecimal commission = parseDecimal(m.getValue(row,
                "commission", "referral fee percentage"));
        BigDecimal tcsAmount = parseDecimal(m.getValue(row,
                "tcs-igst", "tcs igst", "tax collection at source"));
        BigDecimal tdsAmount = parseDecimal(m.getValue(row,
                "tds", "tax deducted at source"));
        BigDecimal settlement = parseDecimal(m.getValue(row,
                "net amount", "total", "settlement amount", "net proceeds"));

        BigDecimal netRevenue = sellingPrice
                .subtract(marketplaceFee.abs())
                .subtract(shippingFee.abs())
                .subtract(commission.abs());

        String sku = m.getValue(row, "sku", "seller sku", "merchant sku");
        String productName = m.getValue(row, "description", "product name", "item description");
        String qty = m.getValue(row, "quantity", "qty", "units");
        String status = m.getValue(row, "fulfillment", "order status", "status");
        String returnStatus = m.getValue(row, "return status", "return type");
        String city = m.getValue(row, "city", "buyer city");
        String state = m.getValue(row, "state", "buyer state");
        String pincode = m.getValue(row, "postal code", "pincode", "pin code");
        String paymentMode = m.getValue(row, "payment type", "payment mode");
        String invoiceNumber = m.getValue(row, "invoice number", "invoice no", "tax invoice number");

        // Store raw data for traceability
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("source", "amazon_settlement");
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
                .commission(commission.abs())
                .tcsAmount(tcsAmount)
                .tdsAmount(tdsAmount)
                .settlementAmount(settlement)
                .netRevenue(netRevenue)
                .orderStatus(status)
                .returnStatus(returnStatus)
                .buyerCity(city)
                .buyerState(state)
                .buyerPincode(pincode)
                .paymentMode(paymentMode)
                .invoiceNumber(invoiceNumber)
                .batchId(batchId)
                .rawData(rawData)
                .build();
    }

    private String[] readRowAsStrings(Row row) {
        if (row == null) return new String[0];
        int lastCell = row.getLastCellNum();
        String[] result = new String[lastCell];
        DataFormatter formatter = new DataFormatter();
        for (int i = 0; i < lastCell; i++) {
            Cell cell = row.getCell(i);
            result[i] = cell != null ? formatter.formatCellValue(cell).trim() : "";
        }
        return result;
    }
}
