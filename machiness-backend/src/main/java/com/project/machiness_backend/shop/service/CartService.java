package com.project.machiness_backend.shop.service;

import com.project.machiness_backend.shop.dto.AddToCartRequest;
import com.project.machiness_backend.shop.dto.CartItemResponse;
import com.project.machiness_backend.shop.dto.CartResponse;

import java.util.UUID;

public interface CartService {

    CartItemResponse addToCart(String userEmail, AddToCartRequest request);

    CartResponse getCart(String userEmail);

    CartItemResponse updateQuantity(String userEmail, UUID cartItemUuid, int quantity);

    void removeFromCart(String userEmail, UUID cartItemUuid);

    void clearCart(String userEmail);
}