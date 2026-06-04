package com.sellerpro.service;

import com.sellerpro.model.Referral;
import com.sellerpro.repository.ReferralRepository;
import com.sellerpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;

    public Map<String, Object> getReferralInfo(String email) {
        String code = referralRepository.findCodeByEmail(email);
        if (code == null) {
            code = generateCode(email);
            referralRepository.saveCode(email, code);
        }

        List<Referral> referred = referralRepository.findByReferrerEmail(email);
        long bonusMonths = referred.stream().filter(Referral::isBonusGranted).count();

        return Map.of(
            "referralCode", code,
            "referralLink", "https://sellerpro.in/signup?ref=" + code,
            "totalReferred", referred.size(),
            "bonusMonthsEarned", bonusMonths,
            "referredUsers", referred
        );
    }

    public Map<String, Object> applyCode(String email, String code) {
        String referrerEmail = referralRepository.findEmailByCode(code);
        if (referrerEmail == null) {
            return Map.of("success", false, "message", "Invalid referral code");
        }
        if (referrerEmail.equals(email)) {
            return Map.of("success", false, "message", "Apna code use nahi kar sakte!");
        }
        boolean alreadyUsed = referralRepository.hasUsedCode(email);
        if (alreadyUsed) {
            return Map.of("success", false, "message", "Already ek code use kar chuke ho");
        }

        Referral referral = Referral.builder()
            .referrerEmail(referrerEmail)
            .refereeEmail(email)
            .appliedAt(LocalDateTime.now())
            .bonusGranted(false)
            .build();
        referralRepository.save(referral);

        return Map.of("success", true, "message", "Referral code applied! Dono ko 1 mahina free milega jab subscription activate hogi.");
    }

    public List<Map<String, Object>> getLeaderboard() {
        return referralRepository.findTopReferrers(10);
    }

    private String generateCode(String email) {
        String prefix = email.split("@")[0].toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (prefix.length() > 6) prefix = prefix.substring(0, 6);
        return prefix + String.format("%04d", new Random().nextInt(9999));
    }
}
