package com.sellerpro.repository;

import com.sellerpro.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findTopByUserEmailAndStatusOrderByCreatedAtDesc(String email, String status);

    List<Subscription> findByUserEmail(String email);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE'")
    long countActiveSubscriptions();

    @Query("SELECT SUM(p.price) FROM Subscription s JOIN s.plan p WHERE s.status = 'ACTIVE'")
    Double sumMonthlyRevenue();

    @Query("SELECT s FROM Subscription s WHERE s.endDate < :today AND s.status = 'ACTIVE'")
    List<Subscription> findExpiredSubscriptions(LocalDate today);

    @Query(value = """
        SELECT u.email, u.name, p.display_name as plan, s.start_date, s.end_date, s.status
        FROM subscriptions s
        JOIN users u ON u.email = s.user_email
        JOIN plans p ON p.id = s.plan_id
        ORDER BY s.created_at DESC
        """, nativeQuery = true)
    List<Object[]> findAllWithUserDetails();
}
