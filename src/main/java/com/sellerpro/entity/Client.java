package com.sellerpro.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "business_name")
    private String businessName;

    private String gstin;
    private String pan;
    private String address;
    private String city;
    private String state;
    private String pincode;

    @Column(name = "amazon_seller_id")
    private String amazonSellerId;

    @Column(name = "flipkart_seller_id")
    private String flipkartSellerId;

    @Column(name = "meesho_seller_id")
    private String meeshoSellerId;

    @Column(name = "nykaa_seller_id")
    private String nykaaSellerId;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "onboarding_complete", nullable = false)
    @Builder.Default
    private Boolean onboardingComplete = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Alert> alerts = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
