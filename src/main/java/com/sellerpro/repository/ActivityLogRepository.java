package com.sellerpro.repository;

import com.sellerpro.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<ActivityLog> findByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);

    List<ActivityLog> findByActionOrderByCreatedAtDesc(String action);

    @Query("SELECT a FROM ActivityLog a WHERE a.action = :action AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentByAction(@Param("action") String action, @Param("since") LocalDateTime since);

    @Query("SELECT a FROM ActivityLog a WHERE a.status = 'FAILURE' ORDER BY a.createdAt DESC")
    Page<ActivityLog> findAllFailures(Pageable pageable);

    @Query("SELECT a.action, COUNT(a) FROM ActivityLog a WHERE a.createdAt >= :since GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> getActionStats(@Param("since") LocalDateTime since);
}
