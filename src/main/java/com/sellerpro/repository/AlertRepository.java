package com.sellerpro.repository;

import com.sellerpro.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByClientIdAndIsReadFalseAndIsDismissedFalseOrderByCreatedAtDesc(Long clientId);

    Page<Alert> findByClientIdAndIsDismissedFalseOrderByCreatedAtDesc(Long clientId, Pageable pageable);

    Long countByClientIdAndIsReadFalse(Long clientId);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.client.id = :clientId AND a.isRead = false")
    void markAllReadByClientId(@Param("clientId") Long clientId);

    List<Alert> findByClientIdAndAlertTypeAndAutoResolvedFalse(Long clientId, String alertType);
}
