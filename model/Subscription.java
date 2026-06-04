package com.sellerpro.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    // ACTIVE, EXPIRED, SUSPENDED, CANCELLED
    @Column(nullable = false)
    private String status = "ACTIVE";

    private LocalDate startDate;
    private LocalDate endDate;

    // manually extended by admin
    private int bonusMonthsApplied = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public boolean isActive() {
        return "ACTIVE".equals(status) && (endDate == null || !LocalDate.now().isAfter(endDate));
    }
}
