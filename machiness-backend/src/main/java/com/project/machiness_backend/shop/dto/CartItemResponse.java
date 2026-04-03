package com.project.machiness_backend.shop.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CartItemResponse {

    private UUID cartItemUuid;
    private UUID machineUuid;
    private String machineName;
    private String machineImageUrl;
    private Double pricePerUnit;
    private Integer quantity;
    private Double totalPrice;
    private LocalDateTime addedAt;
}