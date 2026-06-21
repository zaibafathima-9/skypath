package com.skypath.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException exception) {
        Map<String, Object> response = Map.of(
                "timestamp", Instant.now().toString(),
                "status", exception.getStatus().value(),
                "error", exception.getStatus().getReasonPhrase(),
                "message", exception.getMessage()
        );

        return ResponseEntity
                .status(exception.getStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(Exception exception) {
        Map<String, Object> response = Map.of(
                "timestamp", Instant.now().toString(),
                "status", 500,
                "error", "Internal Server Error",
                "message", "Unexpected server error"
        );

        return ResponseEntity
                .internalServerError()
                .body(response);
    }
}