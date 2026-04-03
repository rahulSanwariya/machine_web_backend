package com.project.machiness_backend.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── AppException ──────────────────────────────────────────────────────
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

    // ── @Valid failures ───────────────────────────────────────────────────
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

    // ── File size exceed ──────────────────────────────────────────────────
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSize(MaxUploadSizeExceededException ex,
                                                       HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .status(400)
                .error("FILE_TOO_LARGE")
                .message("File size exceeds maximum allowed limit of 10MB")
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(body, org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    // ── RuntimeException ──────────────────────────────────────────────────
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex,
                                                       HttpServletRequest request) {
        log.error("RuntimeException: ", ex);
        ErrorResponse body = ErrorResponse.builder()
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())
                .error(ErrorCode.INTERNAL_SERVER_ERROR.name())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(body, ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
    }

    // ── Generic Exception ─────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                       HttpServletRequest request) {
        log.error("Exception: ", ex);
        ErrorResponse body = ErrorResponse.builder()
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())
                .error(ErrorCode.INTERNAL_SERVER_ERROR.name())
                .message(ex.getMessage()) // ← actual message dikhayega
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(body, ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
    }
}