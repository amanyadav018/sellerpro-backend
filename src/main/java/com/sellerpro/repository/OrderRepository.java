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

    @Query("SELECT o.buyerCity, o.buyerState, COUNT(o), SUM(o.netRevenue), AVG(o.netRevenue) " +
           "FROM Order o WHERE o.client.user.email = :email AND o.orderDate >= :from " +
           "GROUP BY o.buyerCity, o.buyerState ORDER BY COUNT(o) DESC")
    List<Object[]> findCityOrderStats(@Param("email") String email, @Param("from") LocalDate from);

    void deleteByClientIdAndBatchId(Long clientId, String batchId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.client.user.email = :email")
    Long countByUserEmail(@Param("email") String email);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.client.user.email = :email AND o.platform = :platform")
    Long countByUserEmailAndPlatform(@Param("email") String email, @Param("platform") String platform);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.client.user.email = :email AND o.orderDate = :today")
    Long countTodayOrders(@Param("email") String email, @Param("today") LocalDate today);

    @Query("SELECT SUM(o.netRevenue) FROM Order o WHERE o.client.user.email = :email AND o.orderDate = :today")
    BigDecimal sumTodayRevenue(@Param("email") String email, @Param("today") LocalDate today);

    @Query("SELECT o.platform, COUNT(o), SUM(o.netRevenue) FROM Order o WHERE o.client.user.email = :email " +
           "AND o.orderDate BETWEEN :from AND :to GROUP BY o.platform")
    List<Object[]> findChannelStats(@Param("email") String email, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m'), COUNT(o), SUM(o.netRevenue) " +
           "FROM Order o WHERE o.client.user.email = :email AND o.orderDate >= :from GROUP BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m')")
    List<Object[]> findMonthlyTrend(@Param("email") String email, @Param("from") LocalDate from);

    @Query("SELECT FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m'), o.platform, COUNT(o), SUM(o.netRevenue) " +
           "FROM Order o WHERE o.client.user.email = :email AND o.orderDate >= :from GROUP BY FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m'), o.platform")
    List<Object[]> findMonthlyTrendByPlatform(@Param("email") String email, @Param("from") LocalDate from);

    @Query("SELECT o.platform FROM Order o WHERE o.client.user.email = :email " +
           "GROUP BY o.platform ORDER BY SUM(o.netRevenue) DESC")
    List<String> findTopPlatformByRevenue(@Param("email") String email);

    @Query("SELECT o.sku, o.productName, SUM(o.quantity), SUM(o.netRevenue) FROM Order o " +
           "WHERE o.client.user.email = :email AND o.orderDate BETWEEN :from AND :to " +
           "GROUP BY o.sku, o.productName ORDER BY SUM(o.netRevenue) DESC")
    List<Object[]> findTopSkus(@Param("email") String email, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT o.sku, o.productName, SUM(o.quantity), SUM(o.netRevenue) FROM Order o " +
           "WHERE o.client.user.email = :email AND o.platform = :platform AND o.orderDate BETWEEN :from AND :to " +
           "GROUP BY o.sku, o.productName ORDER BY SUM(o.netRevenue) DESC")
    List<Object[]> findTopSkusByPlatform(@Param("email") String email, @Param("platform") String platform, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COUNT(o), SUM(o.netRevenue), AVG(o.netRevenue) FROM Order o WHERE o.client.user.email = :email")
    Object[] findWebsiteRevenueSummary(@Param("email") String email);

    @Query("SELECT CAST(SUM(CASE WHEN o.returnStatus IS NOT NULL AND o.returnStatus != '' THEN 1 ELSE 0 END) AS double) / " +
           "NULLIF(COUNT(o), 0) * 100 FROM Order o WHERE o.client.user.email = :email")
    Double calcReturnRate(@Param("email") String email);
}
