package com.sellerpro.repository;

import com.sellerpro.model.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {

    List<AlertRule> findByUserEmail(String email);

    List<AlertRule> findByUserEmailAndEnabled(String email, boolean enabled);

    Optional<AlertRule> findByIdAndUserEmail(Long id, String email);

    @Transactional
    void deleteByIdAndUserEmail(Long id, String email);

    @Query(value = """
        SELECT id, metric, condition, threshold, last_triggered_at, is_read
        FROM alert_rules
        WHERE user_email = :email AND last_triggered_at IS NOT NULL
        ORDER BY last_triggered_at DESC
        LIMIT 20
        """, nativeQuery = true)
    List<Map<String, Object>> findTriggeredAlerts(String email);

    @Modifying
    @Transactional
    @Query("UPDATE AlertRule a SET a.read = true WHERE a.id = :id AND a.userEmail = :email")
    void markAlertRead(Long id, String email);
}
