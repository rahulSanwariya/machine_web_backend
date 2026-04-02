package com.project.machiness_backend.auth.config.service;

import com.project.machiness_backend.auth.config.dto.*;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    ForgotPasswordResponse sendResetLink(ForgotPasswordRequest request);

    ForgotPasswordResponse resetPassword(ResetPasswordRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    String logout(String accessToken);
}
