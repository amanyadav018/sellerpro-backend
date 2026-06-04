package com.sellerpro.service;

import com.sellerpro.dto.OrderResponse;
import com.sellerpro.entity.Order;
import com.sellerpro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public List<OrderResponse> getOrders(Long clientId, String platform, LocalDate from, LocalDate to) {
        List<Order> orders;

        if (platform != null && from != null && to != null) {
            orders = orderRepository.findByClientIdAndOrderDateBetween(clientId, from, to)
                    .stream()
                    .filter(o -> platform.equalsIgnoreCase(o.getPlatform()))
                    .collect(Collectors.toList());
        } else if (platform != null) {
            orders = orderRepository.findByClientIdAndPlatform(clientId, platform.toUpperCase());
        } else if (from != null && to != null) {
            orders = orderRepository.findByClientIdAndOrderDateBetween(clientId, from, to);
        } else {
            orders = orderRepository.findByClientIdAndOrderDateBetween(
                    clientId,
                    LocalDate.now().minusMonths(3),
                    LocalDate.now()
            );
        }

        return orders.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<Object[]> getSkuRanking(Long clientId, LocalDate from, LocalDate to) {
        return orderRepository.getSkuRanking(clientId, from, to);
    }

    public List<Object[]> getPlatformBreakdown(Long clientId, LocalDate from, LocalDate to) {
        return orderRepository.getPlatformBreakdown(clientId, from, to);
    }

    public List<Object[]> getOrdersByState(Long clientId) {
        return orderRepository.getOrdersByState(clientId);
    }

    private OrderResponse toResponse(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .platform(o.getPlatform())
                .orderId(o.getOrderId())
                .orderDate(o.getOrderDate())
                .sku(o.getSku())
                .productName(o.getProductName())
                .quantity(o.getQuantity())
                .sellingPrice(o.getSellingPrice())
                .netRevenue(o.getNetRevenue())
                .settlementAmount(o.getSettlementAmount())
                .orderStatus(o.getOrderStatus())
                .returnStatus(o.getReturnStatus())
                .buyerState(o.getBuyerState())
                .paymentMode(o.getPaymentMode())
                .build();
    }
}
