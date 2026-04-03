package com.project.machiness_backend.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ── User Errors ───────────────────────────────────────────────────────
    USER_NOT_FOUND          (HttpStatus.NOT_FOUND,             "User not found"),
    EMAIL_ALREADY_EXISTS    (HttpStatus.CONFLICT,              "Email already in use"),
    PHONE_ALREADY_EXISTS    (HttpStatus.CONFLICT,              "Phone number already in use"),
    INVALID_CREDENTIALS     (HttpStatus.UNAUTHORIZED,          "Invalid email or password"),

    // ── Token Errors ──────────────────────────────────────────────────────
    INVALID_TOKEN           (HttpStatus.UNAUTHORIZED,          "Invalid token"),
    TOKEN_EXPIRED           (HttpStatus.UNAUTHORIZED,          "Token expired, please login again"),
    MISSING_TOKEN           (HttpStatus.UNAUTHORIZED,          "Authorization token is missing or malformed"),

    // ── Password Errors ───────────────────────────────────────────────────
    PASSWORD_MISMATCH       (HttpStatus.BAD_REQUEST,           "New password and confirm password do not match"),

    // ── Machine Errors ────────────────────────────────────────────────────
    MACHINE_NOT_FOUND       (HttpStatus.NOT_FOUND,             "Machine not found"),
    IMAGE_NOT_FOUND         (HttpStatus.NOT_FOUND,             "Image not found"),
    IMAGE_SAVE_FAILED       (HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save image"),

    // ── Validation / General ──────────────────────────────────────────────
    VALIDATION_FAILED       (HttpStatus.BAD_REQUEST,           "Validation failed"),
    INTERNAL_SERVER_ERROR   (HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}