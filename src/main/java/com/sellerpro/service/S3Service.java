package com.sellerpro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.enabled:false}")
    private boolean s3Enabled;

    public S3Service(S3Client s3Client, S3Presigner presigner) {
        this.s3Client = s3Client;
        this.presigner = presigner;
    }

    /**
     * Upload file to S3.
     * Returns the S3 key (path) of the uploaded file.
     * If S3 is disabled (dev mode), returns a mock key.
     */
    public String uploadFile(MultipartFile file, String platform, Long clientId) throws Exception {
        if (!s3Enabled) {
            // Dev/local mode — skip actual upload
            return "local/" + platform + "/" + clientId + "/" + file.getOriginalFilename();
        }

        String date = LocalDate.now().toString().replace("-", "/");
        String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String key = "uploads/" + clientId + "/" + platform + "/" + date + "/" + uniqueName;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return key;
    }

    /**
     * Generate a pre-signed URL for temporary download access (15 min).
     */
    public String getPresignedUrl(String key) {
        if (!s3Enabled) return "/files/" + key;

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build())
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * Delete file from S3.
     */
    public void deleteFile(String key) {
        if (!s3Enabled) return;

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }
}
