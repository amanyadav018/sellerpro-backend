package com.sellerpro.controller;

import com.sellerpro.dto.OrderResponse;
import com.sellerpro.entity.Client;
import com.sellerpro.entity.User;
import com.sellerpro.repository.ClientRepository;
import com.sellerpro.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ClientRepository clientRepository;

    /**
     * GET /api/orders?platform=AMAZON&from=2024-01-01&to=2024-03-31
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        List<OrderResponse> orders = orderService.getOrders(client.getId(), platform, from, to);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/sku-ranking?from=2024-01-01&to=2024-03-31
     */
    @GetMapping("/sku-ranking")
    public ResponseEntity<?> getSkuRanking(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        return ResponseEntity.ok(orderService.getSkuRanking(client.getId(), from, to));
    }

    /**
     * GET /api/orders/platform-breakdown?from=2024-01-01&to=2024-03-31
     */
    @GetMapping("/platform-breakdown")
    public ResponseEntity<?> getPlatformBreakdown(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        return ResponseEntity.ok(orderService.getPlatformBreakdown(client.getId(), from, to));
    }

    /**
     * GET /api/orders/state-breakdown
     */
    @GetMapping("/state-breakdown")
    public ResponseEntity<?> getStateBreakdown(@AuthenticationPrincipal User user) {
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Client profile not found"));

        return ResponseEntity.ok(orderService.getOrdersByState(client.getId()));
    }
}
