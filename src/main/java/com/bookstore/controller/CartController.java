package com.bookstore.controller;

import com.bookstore.dto.ApiSuccessResponse;
import com.bookstore.dto.CartItemRequest;
import com.bookstore.dto.CartResponse;
import com.bookstore.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Requirement: Add to Cart interaction (Task 1)
     * HTTP Method: POST
     * Safe: No, Idempotent: No
     */
    @PostMapping("/items")
    public ResponseEntity<ApiSuccessResponse<CartResponse>> addItemToCart(
            @RequestHeader(value = "X-User-ID", required = false, defaultValue = "usr_current_session") String userId,
            @Valid @RequestBody CartItemRequest request) {
        
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            request.setUserId(userId);
        }

        CartResponse cart = cartService.addItemToCart(request);
        return ResponseEntity.ok(new ApiSuccessResponse<>("Item added to cart successfully", cart));
    }

    /**
     * Requirement: View Current Cart
     * HTTP Method: GET
     * Safe: Yes, Idempotent: Yes
     */
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<CartResponse>> getCart(
            @RequestHeader(value = "X-User-ID", required = false, defaultValue = "usr_current_session") String userId) {
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(new ApiSuccessResponse<>("Fetched shopping cart successfully", cart));
    }

    /**
     * Requirement: Clear Cart
     * HTTP Method: DELETE
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader(value = "X-User-ID", required = false, defaultValue = "usr_current_session") String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
