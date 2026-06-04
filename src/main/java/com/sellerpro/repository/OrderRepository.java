package com.sellerpro.repository;

import com.sellerpro.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClientIdAndPlatform(Long clientId, String platform);

    List<Order> findByClientIdAndOrderDateBetween(Long clientId, LocalDate from, LocalDate to);

    Optional<Order> findByClientIdAndPlatformAndOrderId(Long clientId, String platform, String orderId);

    boolean existsByClientIdAndPlatformAndOrderId(Long clientId, String platform, String orderId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.client.id = :clientId AND o.platform = :platform")
    Long countByClientIdAndPlatform(@Param("clientId") Long clientId, @Param("platform") String platform);

    @Query("SELECT SUM(o.netRevenue) FROM Order o WHERE o.client.id = :clientId AND o.orderDate BETWEEN :from AND :to")
    BigDecimal sumNetRevenue(@Param("clientId") Long clientId,
                             @Param("from") LocalDate from,
                             @Param("to") LocalDate to);

    @Query("SELECT o.buyerState, COUNT(o) FROM Order o WHERE o.client.id = :clientId GROUP BY o.buyerState ORDER BY COUNT(o) DESC")
    List<Object[]> getOrdersByState(@Param("clientId") Long clientId);

    @Query("SELECT o.sku, o.productName, SUM(o.quantity) as totalQty, SUM(o.netRevenue) as totalRevenue " +
           "FROM Order o WHERE o.client.id = :clientId AND o.orderDate BETWEEN :from AND :to " +
           "GROUP BY o.sku, o.productName ORDER BY totalRevenue DESC")
    List<Object[]> getSkuRanking(@Param("clientId") Long clientId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to);

    @Query("SELECT o.platform, COUNT(o), SUM(o.netRevenue) FROM Order o WHERE o.client.id = :clientId " +
           "AND o.orderDate BETWEEN :from AND :to GROUP BY o.platform")
    List<Object[]> getPlatformBreakdown(@Param("clientId") Long clientId,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.client.id = :clientId AND o.returnStatus IS NOT NULL AND o.returnStatus != ''")
    Long countReturns(@Param("clientId") Long clientId);

    void deleteByClientIdAndBatchId(Long clientId, String batchId);
}
