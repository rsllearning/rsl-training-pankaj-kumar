package com.rsl.training.subscription.exception;

/**
 * Exception thrown when an unrecognized, invalid, or expired promotional voucher code is provided.
 */
public class InvalidVoucherException extends RuntimeException {

    public InvalidVoucherException(String message) {
        super(message);
    }
}
