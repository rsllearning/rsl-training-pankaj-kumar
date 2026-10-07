package com.bookstore.controller;

import com.bookstore.dto.ApiSuccessResponse;
import com.bookstore.dto.OrderResponse;
import com.bookstore.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final OrderService orderService;

    public UserController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Requirement: Retrieve all orders belonging to a specific user
     * HTTP Method: GET
     * Safe: Yes, Idempotent: Yes
     */
    @GetMapping("/{userId}/orders")
    public ResponseEntity<ApiSuccessResponse<List<OrderResponse>>> getUserOrders(@PathVariable String userId) {
        List<OrderResponse> orders = orderService.getOrdersByCustomerId(userId);
        return ResponseEntity.ok(new ApiSuccessResponse<>("Fetched user orders successfully", orders));
    }
}
