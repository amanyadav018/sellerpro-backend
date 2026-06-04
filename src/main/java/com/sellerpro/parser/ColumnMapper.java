package com.sellerpro.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps various column name formats from different platforms to standard field names.
 * Handles inconsistencies like "Order ID" vs "order_id" vs "OrderId" etc.
 */
public class ColumnMapper {

    private final Map<String, Integer> columnIndex = new HashMap<>();

    public ColumnMapper(String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            String normalized = normalize(headers[i]);
            columnIndex.put(normalized, i);
        }
    }

    public Integer getIndex(String... possibleNames) {
        for (String name : possibleNames) {
            Integer idx = columnIndex.get(normalize(name));
            if (idx != null) return idx;
        }
        return null;
    }

    public String getValue(String[] row, String... possibleNames) {
        Integer idx = getIndex(possibleNames);
        if (idx == null || idx >= row.length) return null;
        String val = row[idx];
        return (val == null || val.isBlank()) ? null : val.trim();
    }

    public String getValueOrDefault(String[] row, String defaultVal, String... possibleNames) {
        String val = getValue(row, possibleNames);
        return val != null ? val : defaultVal;
    }

    private String normalize(String col) {
        if (col == null) return "";
        return col.toLowerCase()
                  .replaceAll("[^a-z0-9]", "");
    }
}
