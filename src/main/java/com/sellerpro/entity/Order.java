package com.sellerpro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, length = 30)
    private String platform;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    private String sku;

    @Column(name = "product_name", columnDefinition = "TEXT")
    private String productName;

    private String category;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal mrp;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "marketplace_fee", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal marketplaceFee = BigDecimal.ZERO;

    @Column(name = "shipping_fee", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal commission = BigDecimal.ZERO;

    @Column(name = "tcs_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal tcsAmount = BigDecimal.ZERO;

    @Column(name = "tds_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal tdsAmount = BigDecimal.ZERO;

    @Column(name = "settlement_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal settlementAmount = BigDecimal.ZERO;

    @Column(name = "net_revenue", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal netRevenue = BigDecimal.ZERO;

    @Column(name = "order_status", length = 50)
    private String orderStatus;

    @Column(name = "return_status", length = 50)
    private String returnStatus;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "return_reason", columnDefinition = "TEXT")
    private String returnReason;

    @Column(name = "buyer_city")
    private String buyerCity;

    @Column(name = "buyer_state")
    private String buyerState;

    @Column(name = "buyer_pincode")
    private String buyerPincode;

    @Column(name = "payment_mode")
    private String paymentMode;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "batch_id")
    private String batchId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", columnDefinition = "jsonb")
    private Map<String, Object> rawData;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isReturn() {
        return returnStatus != null && !returnStatus.isBlank();
    }
}
