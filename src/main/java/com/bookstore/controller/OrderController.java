package com.bookstore.controller;

import com.bookstore.dto.ApiSuccessResponse;
import com.bookstore.dto.OrderCreateRequest;
import com.bookstore.dto.OrderResponse;
import com.bookstore.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Requirement: Place a new book order
     * HTTP Method: POST
     * Safe: No, Idempotent: No
     */
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<OrderResponse>> placeOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId,
            @Valid @RequestBody OrderCreateRequest request) {
        
        OrderResponse orderResponse = orderService.createOrder(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderResponse.getOrderId())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiSuccessResponse<>("Order placed successfully", orderResponse));
    }

    /**
     * Requirement: Fetch details of an order by ID
     * HTTP Method: GET
     * Safe: Yes, Idempotent: Yes
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<OrderResponse>> getOrderById(@PathVariable String id) {
        OrderResponse orderResponse = orderService.getOrderById(id);
        return ResponseEntity.ok(new ApiSuccessResponse<>("Fetched order details successfully", orderResponse));
    }
}
