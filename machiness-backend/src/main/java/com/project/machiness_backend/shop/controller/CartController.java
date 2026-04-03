package com.project.machiness_backend.shop.controller;

import com.project.machiness_backend.shop.dto.AddToCartRequest;
import com.project.machiness_backend.shop.dto.CartItemResponse;
import com.project.machiness_backend.shop.dto.CartResponse;
import com.project.machiness_backend.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ── Add to Cart ───────────────────────────────────────────────────────
    @PostMapping("/add")
    public ResponseEntity<CartItemResponse> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(userDetails.getUsername(), request));
    }

    // ── Get My Cart ───────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    // ── Update Quantity ───────────────────────────────────────────────────
    @PatchMapping("/{cartItemUuid}/quantity")
    public ResponseEntity<CartItemResponse> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID cartItemUuid,
            @RequestParam int quantity) {
        return ResponseEntity.ok(
                cartService.updateQuantity(userDetails.getUsername(), cartItemUuid, quantity));
    }

    // ── Remove Item ───────────────────────────────────────────────────────
    @DeleteMapping("/{cartItemUuid}")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID cartItemUuid) {
        cartService.removeFromCart(userDetails.getUsername(), cartItemUuid);
        return ResponseEntity.noContent().build();
    }

    // ── Clear Cart ────────────────────────────────────────────────────────
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}