package com.sellerpro.repository;

import com.sellerpro.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<Alert> findByClientIdAndReadFalseOrderByCreatedAtDesc(Long clientId);

    List<Alert> findByClientIdAndSeverityOrderByCreatedAtDesc(Long clientId, String severity);

    @Query("SELECT a FROM Alert a WHERE a.clientId = :clientId ORDER BY a.createdAt DESC")
    List<Alert> findTop20ByClientId(@Param("clientId") Long clientId);

    long countByClientIdAndReadFalse(Long clientId);

    @Modifying
    @Query("UPDATE Alert a SET a.read = true WHERE a.clientId = :clientId")
    void markAllAsRead(@Param("clientId") Long clientId);

    @Modifying
    @Query("UPDATE Alert a SET a.read = true WHERE a.id = :id AND a.clientId = :clientId")
    void markAsRead(@Param("id") Long id, @Param("clientId") Long clientId);

    // Check if same type alert already fired in last 24h (avoid duplicates)
    boolean existsByClientIdAndTypeAndCreatedAtAfter(Long clientId, String type, LocalDateTime after);
}
