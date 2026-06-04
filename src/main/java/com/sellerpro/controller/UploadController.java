package com.sellerpro.controller;

import com.sellerpro.entity.Client;
import com.sellerpro.entity.FileUpload;
import com.sellerpro.entity.User;
import com.sellerpro.repository.ClientRepository;
import com.sellerpro.repository.FileUploadRepository;
import com.sellerpro.service.FileParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileParserService fileParserService;
    private final FileUploadRepository fileUploadRepository;
    private final ClientRepository clientRepository;

    /**
     * POST /api/upload?platform=AMAZON
     * Accepts multipart file, parses and saves orders.
     */
    @PostMapping
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("platform") String platform,
            @AuthenticationPrincipal User user) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        try {
            FileUpload result = fileParserService.processUpload(file, platform, client);
            return ResponseEntity.ok(Map.of(
                    "batchId", result.getBatchId() != null ? result.getBatchId() : "",
                    "status", result.getStatus().name(),
                    "totalRows", result.getTotalRows() != null ? result.getTotalRows() : 0,
                    "successCount", result.getSuccessCount() != null ? result.getSuccessCount() : 0,
                    "skipCount", result.getSkipCount() != null ? result.getSkipCount() : 0,
                    "errorCount", result.getErrorCount() != null ? result.getErrorCount() : 0,
                    "errorLog", result.getErrorLog() != null ? result.getErrorLog() : ""
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * GET /api/upload/history
     * Returns list of past uploads for logged-in user.
     */
    @GetMapping("/history")
    public ResponseEntity<List<FileUpload>> getUploadHistory(@AuthenticationPrincipal User user) {
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        List<FileUpload> history = fileUploadRepository.findByClientIdOrderByUploadedAtDesc(client.getId());
        return ResponseEntity.ok(history);
    }

    /**
     * DELETE /api/upload/{batchId}
     * Rolls back an upload — deletes all orders from that batch.
     */
    @DeleteMapping("/{batchId}")
    public ResponseEntity<?> deleteUpload(
            @PathVariable String batchId,
            @AuthenticationPrincipal User user) {

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        fileParserService.deleteUpload(batchId, client.getId());
        return ResponseEntity.ok(Map.of("message", "Upload deleted successfully"));
    }
}
