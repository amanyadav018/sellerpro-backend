package com.sellerpro.repository;

import com.sellerpro.entity.GstEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GstEntryRepository extends JpaRepository<GstEntry, Long> {

    List<GstEntry> findByClientIdAndReturnPeriod(Long clientId, String returnPeriod);

    List<GstEntry> findByClientIdAndReturnPeriodOrderByOrderDateAsc(Long clientId, String returnPeriod);

    @Query("SELECT SUM(g.totalGst) FROM GstEntry g WHERE g.clientId = :clientId AND g.returnPeriod = :period")
    BigDecimal sumTotalGstByClientAndPeriod(@Param("clientId") Long clientId, @Param("period") String period);

    @Query("SELECT SUM(g.igst) FROM GstEntry g WHERE g.clientId = :clientId AND g.returnPeriod = :period")
    BigDecimal sumIgstByClientAndPeriod(@Param("clientId") Long clientId, @Param("period") String period);

    @Query("SELECT SUM(g.cgst) FROM GstEntry g WHERE g.clientId = :clientId AND g.returnPeriod = :period")
    BigDecimal sumCgstByClientAndPeriod(@Param("clientId") Long clientId, @Param("period") String period);

    @Query("SELECT SUM(g.sgst) FROM GstEntry g WHERE g.clientId = :clientId AND g.returnPeriod = :period")
    BigDecimal sumSgstByClientAndPeriod(@Param("clientId") Long clientId, @Param("period") String period);

    boolean existsByClientIdAndOrderId(Long clientId, String orderId);

    void deleteByClientIdAndReturnPeriod(Long clientId, String returnPeriod);

    List<GstEntry> findByClientIdAndReturnPeriodAndBuyerState(Long clientId, String returnPeriod, String buyerState);
}
