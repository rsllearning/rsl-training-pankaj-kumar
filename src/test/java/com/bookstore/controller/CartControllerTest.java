package com.bookstore.controller;

import com.bookstore.dto.CartItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /v1/cart/items - Successfully Add Item to Cart (Task 1 Lifecycle)")
    void testAddItemToCartSuccess() throws Exception {
        CartItemRequest request = new CartItemRequest("usr_test_123", "bk_849201", 1);

        mockMvc.perform(post("/v1/cart/items")
                .header("X-User-ID", "usr_test_123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.totalItems", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.subtotal", notNullValue()));
    }

    @Test
    @DisplayName("POST /v1/cart/items - Exceed Stock Quantity should return 409 Conflict")
    void testAddItemToCartExceedStock() throws Exception {
        CartItemRequest request = new CartItemRequest("usr_test_456", "bk_low_stock", 99);

        mockMvc.perform(post("/v1/cart/items")
                .header("X-User-ID", "usr_test_456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.errorCode", is("ERR_INSUFFICIENT_STOCK")));
    }

    @Test
    @DisplayName("GET /v1/cart - Fetch User Cart")
    void testGetCart() throws Exception {
        mockMvc.perform(get("/v1/cart")
                .header("X-User-ID", "usr_test_123")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.userId", is("usr_test_123")));
    }
}
