package com.project.machiness_backend.auth.config.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── AppException (saare custom errors yahan aate hain) ────────────────
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex,
                                                            HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();
        ErrorResponse body = ErrorResponse.builder()
                .status(code.getHttpStatus().value())
                .error(code.name())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(body, code.getHttpStatus());
    }

    // ── @Valid / @Validated failures ──────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse(ErrorCode.VALIDATION_FAILED.getDefaultMessage());

        ErrorResponse body = ErrorResponse.builder()
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus().value())
                .error(ErrorCode.VALIDATION_FAILED.name())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(body, ErrorCode.VALIDATION_FAILED.getHttpStatus());
    }

    // ── Unexpected errors ─────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                       HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())
                .error(ErrorCode.INTERNAL_SERVER_ERROR.name())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(body, ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
    }
}