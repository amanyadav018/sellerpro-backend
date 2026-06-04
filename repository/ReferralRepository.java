package com.sellerpro.repository;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ReferralRepository — used by AdminStatsService for referral count.
 * Full implementation is in Phase 6 (Referral.java + ReferralRepository).
 * This stub is needed so Phase 7 compiles standalone.
 *
 * If you have Phase 6 already merged, DELETE this file — use the one from Phase 6.
 */
public interface ReferralRepository extends JpaRepository<Object, Long> {
    // count() inherited from JpaRepository
}
