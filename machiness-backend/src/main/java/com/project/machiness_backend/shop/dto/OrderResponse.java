package com.project.machiness_backend.shop.dto;

import com.project.machiness_backend.shop.entity.Order.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {

    private UUID orderUuid;
    private UUID machineUuid;
    private String machineName;
    private String machineImageUrl;
    private Double pricePerUnit;
    private Integer quantity;
    private Double totalPrice;
    private OrderStatus status;
    private LocalDateTime placedAt;
    private LocalDateTime updatedAt;
}