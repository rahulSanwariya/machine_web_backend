package com.project.machiness_backend.shop.controller;

import com.project.machiness_backend.shop.dto.OrderResponse;
import com.project.machiness_backend.shop.dto.PlaceOrderRequest;
import com.project.machiness_backend.shop.entity.Order.OrderStatus;
import com.project.machiness_backend.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── Buy Now — seedha ek machine ka order ──────────────────────────────
    @PostMapping("/buy-now")
    public ResponseEntity<OrderResponse> buyNow(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(userDetails.getUsername(), request));
    }

    // ── Cart se saare items ka order ──────────────────────────────────────
    @PostMapping("/place-from-cart")
    public ResponseEntity<List<OrderResponse>> placeOrderFromCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.placeOrderFromCart(userDetails.getUsername()));
    }

    // ── My Orders ─────────────────────────────────────────────────────────
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getMyOrders(userDetails.getUsername()));
    }

    // ── Single Order ──────────────────────────────────────────────────────
    @GetMapping("/{orderUuid}")
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID orderUuid) {
        return ResponseEntity.ok(orderService.getOrder(userDetails.getUsername(), orderUuid));
    }

    // ── Update Status (Admin use) ─────────────────────────────────────────
    @PatchMapping("/{orderUuid}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID orderUuid,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderUuid, status));
    }
}