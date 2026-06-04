package com.sellerpro.repository;

import com.sellerpro.entity.Subscription;
import com.sellerpro.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    List<Subscription> findByUserId(Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE' AND s.endDate >= :today")
    Optional<Subscription> findActiveSubscription(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate BETWEEN :today AND :warningDate")
    List<Subscription> findSubscriptionsExpiringSoon(
            @Param("today") LocalDate today,
            @Param("warningDate") LocalDate warningDate);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate < :today")
    List<Subscription> findExpiredActiveSubscriptions(@Param("today") LocalDate today);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE'")
    Long countActiveSubscriptions();

    @Query("SELECT SUM(s.paymentAmount) FROM Subscription s WHERE s.createdAt >= :startDate AND s.status != 'CANCELLED'")
    Double sumRevenueFrom(LocalDate startDate);
}
