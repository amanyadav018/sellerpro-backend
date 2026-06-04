package com.sellerpro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "referrals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referrer_email", nullable = false)
    private String referrerEmail;

    @Column(name = "referee_email", nullable = false, unique = true)
    private String refereeEmail;

    private LocalDateTime appliedAt;

    @Column(name = "bonus_granted")
    private boolean bonusGranted = false;

    private LocalDateTime bonusGrantedAt;
}
