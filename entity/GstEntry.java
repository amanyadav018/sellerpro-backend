package com.sellerpro.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gst_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GstEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private String orderId;

    private String platform; // AMAZON, FLIPKART, MEESHO, NYKAA, WEBSITE

    private LocalDate orderDate;

    private String buyerState;

    private BigDecimal taxableAmount;

    private BigDecimal cgst;

    private BigDecimal sgst;

    private BigDecimal igst;

    private BigDecimal totalGst;

    private String gstRate; // 5%, 12%, 18%, 28%

    private String hsnCode;

    private String invoiceNumber;

    private String returnPeriod; // e.g. "2024-01" for Jan 2024

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
