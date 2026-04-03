package com.project.machiness_backend.adminpanel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineResponse {

    private UUID uuid;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private boolean isAvailable;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
