package com.rsl.training.subscription.exception;

import java.time.Instant;

/**
 * Standardized API error payload record.
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    Instant timestamp
) {
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, Instant.now());
    }
}
