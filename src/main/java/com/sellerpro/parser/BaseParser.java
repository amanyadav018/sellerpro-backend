package com.sellerpro.parser;

import com.sellerpro.entity.Client;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Abstract base class for all platform parsers.
 * Provides common utilities for parsing dates, decimals, etc.
 */
public abstract class BaseParser {

    protected static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("d-MMM-yy"),
        DateTimeFormatter.ofPattern("d MMM yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    /**
     * Parse the uploaded file and return ParseResult.
     * Each platform implements this method.
     */
    public abstract ParseResult parse(MultipartFile file, Client client) throws Exception;

    /**
     * Platform name this parser handles.
     */
    public abstract String getPlatform();

    protected String generateBatchId() {
        return getPlatform().toUpperCase() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    protected LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        dateStr = dateStr.trim();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    protected BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        try {
            // Remove currency symbols, commas, spaces
            String cleaned = value.replaceAll("[₹,$,\\s]", "").replaceAll(",", "");
            if (cleaned.isEmpty() || cleaned.equals("-")) return BigDecimal.ZERO;
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    protected Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return 1;
        try {
            return Integer.parseInt(value.trim().replaceAll(",", ""));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    protected boolean isSkipRow(String[] row) {
        if (row == null || row.length == 0) return true;
        boolean allEmpty = true;
        for (String cell : row) {
            if (cell != null && !cell.isBlank()) {
                allEmpty = false;
                break;
            }
        }
        return allEmpty;
    }
}
