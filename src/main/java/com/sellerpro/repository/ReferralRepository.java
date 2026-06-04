package com.sellerpro.repository;

import com.sellerpro.model.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ReferralRepository extends JpaRepository<Referral, Long> {

    List<Referral> findByReferrerEmail(String email);

    @Query(value = "SELECT referral_code FROM users WHERE email = :email", nativeQuery = true)
    String findCodeByEmail(String email);

    @Query(value = "SELECT email FROM users WHERE referral_code = :code", nativeQuery = true)
    String findEmailByCode(String code);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET referral_code = :code WHERE email = :email", nativeQuery = true)
    void saveCode(String email, String code);

    @Query(value = "SELECT COUNT(*) > 0 FROM referrals WHERE referee_email = :email", nativeQuery = true)
    boolean hasUsedCode(String email);

    @Query(value = """
        SELECT u.email, u.name, COUNT(r.id) as referral_count,
               SUM(CASE WHEN r.bonus_granted THEN 1 ELSE 0 END) as bonus_earned
        FROM referrals r
        JOIN users u ON u.email = r.referrer_email
        GROUP BY u.email, u.name
        ORDER BY referral_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Map<String, Object>> findTopReferrers(int limit);
}
