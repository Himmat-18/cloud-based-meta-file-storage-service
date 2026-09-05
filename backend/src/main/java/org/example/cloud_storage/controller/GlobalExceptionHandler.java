package org.example.cloud_storage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException ex) {

        String message = ex.getMessage();

        if (message != null &&
                (message.contains("permission")
                        || message.contains("access")
                        || message.contains("Only OWNER or EDITOR"))) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "status", "403",
                            "error", "Forbidden",
                            "message", message
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "status", "500",
                        "error", "Internal Server Error",
                        "message", message == null
                                ? "Something went wrong"
                                : message
                ));
    }
}