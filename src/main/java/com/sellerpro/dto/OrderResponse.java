package com.sellerpro.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String platform;
    private String orderId;
    private LocalDate orderDate;
    private String sku;
    private String productName;
    private Integer quantity;
    private BigDecimal sellingPrice;
    private BigDecimal netRevenue;
    private BigDecimal settlementAmount;
    private String orderStatus;
    private String returnStatus;
    private String buyerState;
    private String paymentMode;
}
