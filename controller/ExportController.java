package com.sellerpro.controller;

import com.sellerpro.dto.ExportFilter;
import com.sellerpro.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExportController {

    private final ExportService exportService;

    /**
     * GET /api/export/csv?clientId=1&startDate=2024-01-01&endDate=2024-01-31&platform=AMAZON
     */
    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam Long clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String status) throws IOException {

        ExportFilter filter = ExportFilter.builder()
            .clientId(clientId)
            .startDate(startDate)
            .endDate(endDate)
            .platform(platform)
            .status(status)
            .format("csv")
            .build();

        byte[] data = exportService.exportToCsv(filter);
        String filename = buildFilename(clientId, platform, startDate, endDate, "csv");

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .body(data);
    }

    /**
     * GET /api/export/xlsx?clientId=1&startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/xlsx")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam Long clientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String status) throws IOException {

        ExportFilter filter = ExportFilter.builder()
            .clientId(clientId)
            .startDate(startDate)
            .endDate(endDate)
            .platform(platform)
            .status(status)
            .format("xlsx")
            .build();

        byte[] data = exportService.exportToExcel(filter);
        String filename = buildFilename(clientId, platform, startDate, endDate, "xlsx");

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(data);
    }

    private String buildFilename(Long clientId, String platform, LocalDate start, LocalDate end, String ext) {
        String dateStr = (start != null ? start.format(DateTimeFormatter.BASIC_ISO_DATE) : "all") +
            "_" + (end != null ? end.format(DateTimeFormatter.BASIC_ISO_DATE) : "today");
        String platformStr = platform != null ? "_" + platform.toLowerCase() : "";
        return "orders_" + clientId + platformStr + "_" + dateStr + "." + ext;
    }
}
