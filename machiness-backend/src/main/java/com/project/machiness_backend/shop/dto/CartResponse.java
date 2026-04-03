package com.project.machiness_backend.shop.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse {

    private List<CartItemResponse> items;
    private int totalItems;
    private Double cartTotal;
}
