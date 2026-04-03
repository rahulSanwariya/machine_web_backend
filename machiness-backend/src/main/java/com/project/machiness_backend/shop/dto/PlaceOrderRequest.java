package com.project.machiness_backend.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PlaceOrderRequest {

    @NotNull(message = "Machine UUID is required")
    private UUID machineUuid;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1;
}
