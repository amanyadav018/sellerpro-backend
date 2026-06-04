package com.sellerpro.repository;

import com.sellerpro.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findByUserEmailOrderByCreatedAtDesc(String email, Pageable pageable);

    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ActivityLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    @Query(value = """
        SELECT action, COUNT(*) as count
        FROM activity_logs
        WHERE created_at >= NOW() - INTERVAL '30 days'
        GROUP BY action
        ORDER BY count DESC
        """, nativeQuery = true)
    List<Object[]> findActionSummaryLast30Days();
}
