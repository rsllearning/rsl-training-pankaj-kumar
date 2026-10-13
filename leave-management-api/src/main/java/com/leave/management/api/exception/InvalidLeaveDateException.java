package com.leave.management.api.exception;

public class InvalidLeaveDateException extends RuntimeException {
    public InvalidLeaveDateException(String message) {
        super(message);
    }
}
