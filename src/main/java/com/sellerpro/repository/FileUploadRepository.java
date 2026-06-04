package com.sellerpro.repository;

import com.sellerpro.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {

    List<FileUpload> findByClientIdOrderByUploadedAtDesc(Long clientId);

    Optional<FileUpload> findByBatchId(String batchId);

    List<FileUpload> findByClientIdAndPlatformOrderByUploadedAtDesc(Long clientId, String platform);

    boolean existsByClientIdAndBatchId(Long clientId, String batchId);
}
