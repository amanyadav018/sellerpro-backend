package com.sellerpro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "failed_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    private double amount;

    private String planName;

    // RAZORPAY, MANUAL, UPI
    private String paymentMethod;

    private String failureReason;

    private String transactionId;

    // PENDING_RETRY, RESOLVED, ABANDONED
    private String status = "PENDING_RETRY";

    private LocalDateTime attemptedAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() { attemptedAt = LocalDateTime.now(); }
}
