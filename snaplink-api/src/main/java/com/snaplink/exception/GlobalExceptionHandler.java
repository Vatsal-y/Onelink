package com.snaplink.exception;

import com.snaplink.util.Base62Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized exception handling for all REST controllers.
 * Produces structured JSON error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---- 400 — Validation errors ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", Map.of("errors", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // ---- 404 — Link not found ----
    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(LinkNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(),
                Map.of("shortCode", ex.getShortCode()));
    }

    // ---- 409 — Alias conflict ----
    @ExceptionHandler(AliasConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(AliasConflictException ex) {
        // Generate 3 alternative suggestions
        List<String> suggestions = List.of(
                ex.getAlias() + "-" + Base62Encoder.generateRandomCode().substring(0, 3),
                ex.getAlias() + "-" + Base62Encoder.generateRandomCode().substring(0, 4),
                Base62Encoder.generateRandomCode()
        );

        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(),
                Map.of("alias", ex.getAlias(), "suggestions", suggestions));
    }

    // ---- 410 — Link expired ----
    @ExceptionHandler(LinkExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleExpired(LinkExpiredException ex) {
        return buildResponse(HttpStatus.GONE, ex.getMessage(),
                Map.of("shortCode", ex.getShortCode()));
    }

    // ---- 500 — Catch-all ----
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred", null);
    }

    // ---- Helper ----
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message, Map<String, Object> extra) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("timestamp", Instant.now().toString());

        if (extra != null) {
            body.putAll(extra);
        }
        return ResponseEntity.status(status).body(body);
    }
}
