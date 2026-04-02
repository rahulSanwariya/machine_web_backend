package com.project.machiness_backend.auth.config.service.serviceImpl;

import com.project.machiness_backend.auth.config.dto.*;
import com.project.machiness_backend.auth.config.entity.PasswordResetToken;
import com.project.machiness_backend.auth.config.entity.RefreshToken;
import com.project.machiness_backend.auth.config.entity.User;
import com.project.machiness_backend.auth.config.exception.AppException;
import com.project.machiness_backend.auth.config.exception.ErrorCode;
import com.project.machiness_backend.auth.config.repo.PasswordResetTokenRepository;
import com.project.machiness_backend.auth.config.repo.RefreshTokenRepository;
import com.project.machiness_backend.auth.config.repo.UserRepository;
import com.project.machiness_backend.auth.config.security.JwtUtil;
import com.project.machiness_backend.auth.config.service.AuthService;
import com.project.machiness_backend.auth.config.service.EmailService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final EntityManager entityManager;

    // ── Register → AuthResponse ───────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);

        if (userRepository.existsByNumber(request.getNumber()))
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNumber(request.getNumber());
        userRepository.save(user);

        String accessToken  = jwtUtil.generateToken(user.getEmail());
        String refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ── Login → AuthResponse ──────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);

        String accessToken  = jwtUtil.generateToken(user.getEmail());
        String refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ── Forgot Password Step 1: Email bhejo → reset link ─────────────────
    @Override
    @Transactional
    public ForgotPasswordResponse sendResetLink(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        passwordResetTokenRepository.deleteByUser(user);
        entityManager.flush();

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);

        return ForgotPasswordResponse.builder()
                .success(true)
                .message("Password reset link sent to your email")
                .build();
    }

    // ── Forgot Password Step 2: Token verify → password reset ────────────
    @Override
    @Transactional
    public ForgotPasswordResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (resetToken.isUsed())
            throw new AppException(ErrorCode.INVALID_TOKEN);

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new AppException(ErrorCode.TOKEN_EXPIRED);

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.markAllUsedByUser(user);

        return ForgotPasswordResponse.builder()
                .success(true)
                .message("Password reset successfully. Please login with your new password.")
                .build();
    }

    // ── Refresh Token → AuthResponse ──────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.expireTokenByUser(refreshToken.getUser());
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateToken(user.getEmail());

        return buildAuthResponse(user, newAccessToken, refreshToken.getToken());
    }

    // ── Logout → String ───────────────────────────────────────────────────
    @Override
    @Transactional
    public String logout(String accessToken) {
        if (accessToken == null || !accessToken.startsWith("Bearer "))
            throw new AppException(ErrorCode.MISSING_TOKEN);

        String token = accessToken.substring(7);
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.expireTokenByUser(user);
        refreshTokenRepository.deleteByUser(user);

        return "Logged out successfully";
    }

    // ── Helper: Refresh Token banao ───────────────────────────────────────
    private String createRefreshToken(User user) {
        // Delete karo aur turant flush karo DB mein
        refreshTokenRepository.deleteByUser(user);
        entityManager.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .isExpired(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    // ── Helper: AuthResponse build ────────────────────────────────────────
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .uuid(user.getUuid())
                .name(user.getName())
                .email(user.getEmail())
                .number(user.getNumber())
                .createdAt(user.getCreatedAt())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}