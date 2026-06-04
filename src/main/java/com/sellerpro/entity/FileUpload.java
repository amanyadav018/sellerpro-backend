package com.sellerpro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_uploads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "batch_id", nullable = false, unique = true)
    private String batchId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(nullable = false, length = 20)
    private String platform;

    @Column(name = "s3_key")
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UploadStatus status = UploadStatus.PENDING;

    @Column(name = "total_rows")
    private Integer totalRows;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "skip_count")
    private Integer skipCount;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "error_log", columnDefinition = "TEXT")
    private String errorLog;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public enum UploadStatus {
        PENDING, PROCESSING, SUCCESS, PARTIAL, FAILED
    }
}
