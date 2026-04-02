package com.project.machiness_backend.auth.config.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private UUID uuid;
    private String name;
    private String email;
    private String number;
    private LocalDate createdAt;
    private String accessToken;
    private String refreshToken;

}
