package com.project.machiness_backend.shop.service;

import com.project.machiness_backend.shop.dto.OrderResponse;
import com.project.machiness_backend.shop.dto.PlaceOrderRequest;
import com.project.machiness_backend.shop.entity.Order.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    // Buy Now — seedha single machine ka order
    OrderResponse placeOrder(String userEmail, PlaceOrderRequest request);

    // Cart se saare items ka order
    List<OrderResponse> placeOrderFromCart(String userEmail);

    List<OrderResponse> getMyOrders(String userEmail);

    OrderResponse getOrder(String userEmail, UUID orderUuid);

    OrderResponse updateOrderStatus(UUID orderUuid, OrderStatus status);
}