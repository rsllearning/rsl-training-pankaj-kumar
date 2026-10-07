package com.bookstore.exception;

import com.bookstore.dto.FieldErrorDetail;
import com.bookstore.dto.ProblemDetailResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle InsufficientStockException -> 409 Conflict (RFC 7807 Format)
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ProblemDetailResponse> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest request) {

        String requestId = resolveRequestId(request);
        ProblemDetailResponse problem = ProblemDetailResponse.of(
                "https://api.bookstore.com/v1/errors/INSUFFICIENT_INVENTORY",
                "Insufficient Inventory Conflict",
                HttpStatus.CONFLICT.value(),
                "ERR_INSUFFICIENT_STOCK",
                ex.getMessage(),
                request.getRequestURI(),
                requestId);

        problem.setInvalidItems(ex.getInvalidItems());
        problem.setSuggestedAction(
                "Reduce the requested quantity to available stock or save items to your wishlist for backorder notification.");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Handle Bean Validation Failures -> 422 Unprocessable Entity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String requestId = resolveRequestId(request);
        List<FieldErrorDetail> fieldErrors = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new FieldErrorDetail(
                    error.getField(),
                    error.getRejectedValue(),
                    error.getCode(),
                    error.getDefaultMessage()));
        }

        ProblemDetailResponse problem = ProblemDetailResponse.of(
                "https://api.bookstore.com/v1/errors/VALIDATION_FAILED",
                "Validation Failed",
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "ERR_SCHEMA_VALIDATION",
                String.format(
                        "The payload contains %d validation error(s). Please correct the invalid fields and retry.",
                        fieldErrors.size()),
                request.getRequestURI(),
                requestId);

        problem.setFieldErrors(fieldErrors);
        problem.setSuggestedAction(
                "Ensure all required fields are populated properly with valid types and constraints.");

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    /**
     * Handle ResourceNotFoundException -> 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetailResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        String requestId = resolveRequestId(request);
        ProblemDetailResponse problem = ProblemDetailResponse.of(
                "https://api.bookstore.com/v1/errors/RESOURCE_NOT_FOUND",
                "Resource Not Found",
                HttpStatus.NOT_FOUND.value(),
                "ERR_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                requestId);

        problem.setSuggestedAction("Verify that the target resource identifier is correct and active.");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Handle InvalidOperationException -> 400 Bad Request
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ProblemDetailResponse> handleInvalidOperation(
            InvalidOperationException ex, HttpServletRequest request) {

        String requestId = resolveRequestId(request);
        ProblemDetailResponse problem = ProblemDetailResponse.of(
                "https://api.bookstore.com/v1/errors/BAD_REQUEST",
                "Invalid Operation",
                HttpStatus.BAD_REQUEST.value(),
                "ERR_INVALID_OPERATION",
                ex.getMessage(),
                request.getRequestURI(),
                requestId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handle Generic Unhandled Exceptions -> 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        String requestId = resolveRequestId(request);
        ProblemDetailResponse problem = ProblemDetailResponse.of(
                "https://api.bookstore.com/v1/errors/INTERNAL_SERVER_ERROR",
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "ERR_INTERNAL_SERVER",
                "An unexpected server error occurred. Please contact system support.",
                request.getRequestURI(),
                requestId);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isBlank()) {
            requestId = "req_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        return requestId;
    }
}
