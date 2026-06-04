package com.sellerpro.service;

import com.sellerpro.entity.Client;
import com.sellerpro.entity.FileUpload;
import com.sellerpro.entity.Order;
import com.sellerpro.parser.BaseParser;
import com.sellerpro.parser.ParseResult;
import com.sellerpro.parser.ParserFactory;
import com.sellerpro.repository.FileUploadRepository;
import com.sellerpro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileParserService {

    private final ParserFactory parserFactory;
    private final OrderRepository orderRepository;
    private final FileUploadRepository fileUploadRepository;
    private final S3Service s3Service;

    /**
     * Main entry point: upload file, parse it, save orders.
     */
    @Transactional
    public FileUpload processUpload(MultipartFile file, String platform, Client client) throws Exception {
        // 1. Upload file to S3
        String s3Key = s3Service.uploadFile(file, platform, client.getId());

        // 2. Get correct parser
        BaseParser parser = parserFactory.getParser(platform);

        // 3. Create initial FileUpload record
        FileUpload upload = FileUpload.builder()
                .client(client)
                .fileName(file.getOriginalFilename())
                .platform(platform.toUpperCase())
                .s3Key(s3Key)
                .status(FileUpload.UploadStatus.PROCESSING)
                .build();
        fileUploadRepository.save(upload);

        try {
            // 4. Parse the file
            ParseResult result = parser.parse(file, client);
            upload.setBatchId(result.getBatchId());
            upload.setTotalRows(result.getTotalRows());
            upload.setSkipCount(result.getSkipCount());
            upload.setErrorCount(result.getErrors().size());

            if (!result.getErrors().isEmpty()) {
                upload.setErrorLog(String.join("\n", result.getErrors()));
            }

            // 5. Save orders (skip duplicates)
            List<Order> orders = result.getOrders();
            int saved = 0;
            for (Order order : orders) {
                boolean exists = orderRepository.existsByClientIdAndPlatformAndOrderId(
                        client.getId(), order.getPlatform(), order.getOrderId());
                if (!exists) {
                    orderRepository.save(order);
                    saved++;
                } else {
                    upload.setSkipCount(upload.getSkipCount() + 1);
                }
            }

            upload.setSuccessCount(saved);
            upload.setProcessedAt(LocalDateTime.now());

            // 6. Set final status
            if (result.getErrors().isEmpty()) {
                upload.setStatus(FileUpload.UploadStatus.SUCCESS);
            } else if (saved > 0) {
                upload.setStatus(FileUpload.UploadStatus.PARTIAL);
            } else {
                upload.setStatus(FileUpload.UploadStatus.FAILED);
            }

            log.info("Upload processed: platform={}, file={}, saved={}, errors={}",
                    platform, file.getOriginalFilename(), saved, result.getErrors().size());

        } catch (Exception e) {
            log.error("Upload failed: {}", e.getMessage(), e);
            upload.setStatus(FileUpload.UploadStatus.FAILED);
            upload.setErrorLog(e.getMessage());
            upload.setProcessedAt(LocalDateTime.now());
        }

        return fileUploadRepository.save(upload);
    }

    /**
     * Delete all orders from a batch (undo upload).
     */
    @Transactional
    public void deleteUpload(String batchId, Long clientId) {
        orderRepository.deleteByClientIdAndBatchId(clientId, batchId);
        fileUploadRepository.findByBatchId(batchId).ifPresent(fileUploadRepository::delete);
    }
}
