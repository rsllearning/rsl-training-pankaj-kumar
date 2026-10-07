package com.bookstore.controller;

import com.bookstore.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /v1/orders - Successful Order Creation (201 Created)")
    void testPlaceOrderSuccess() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId("usr_908214");

        OrderItemRequest item1 = new OrderItemRequest("bk_849201", 2, BigDecimal.valueOf(39.99));
        OrderItemRequest item2 = new OrderItemRequest("bk_110934", 1, BigDecimal.valueOf(49.50));
        request.setItems(List.of(item1, item2));

        ShippingAddressRequest shipping = new ShippingAddressRequest();
        shipping.setFullName("Pankaj Kumar");
        shipping.setStreetAddress("456 Silicon Avenue, Suite 100");
        shipping.setCity("Bengaluru");
        shipping.setState("Karnataka");
        shipping.setPostalCode("560100");
        shipping.setCountry("IND");
        shipping.setPhoneNumber("+919876543210");
        request.setShippingAddress(shipping);

        BillingAddressRequest billing = new BillingAddressRequest();
        billing.setSameAsShipping(true);
        request.setBillingAddress(billing);

        PaymentRequest payment = new PaymentRequest("CREDIT_CARD", "tok_visa_4242_sec_998124", "USD");
        request.setPayment(payment);
        request.setNotes("Please leave the package at the front security desk.");

        mockMvc.perform(post("/v1/orders")
                .header("Authorization", "Bearer sample-jwt-token")
                .header("Idempotency-Key", "7b9c2a11-8f56-4c91-a62d-98e3b1c8f420")
                .header("X-Request-ID", "req_a1b2c3d4-e5f6-7890-1234-56789abcdef0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.orderId", notNullValue()))
                .andExpect(jsonPath("$.data.customerId", is("usr_908214")))
                .andExpect(jsonPath("$.data.orderStatus", is("CONFIRMED")))
                .andExpect(jsonPath("$.data.paymentStatus", is("PAID")))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.pricingSummary.totalAmount", notNullValue()))
                .andExpect(jsonPath("$.data.shippingDetails.city", is("Bengaluru")))
                .andExpect(jsonPath("$.data.links.self", notNullValue()));
    }

    @Test
    @DisplayName("POST /v1/orders - Failure Scenario: Requested Quantity Exceeds Stock (409 Conflict)")
    void testPlaceOrderInsufficientStock() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId("usr_908214");

        // Request 10 units of bk_low_stock (which only has 2 in stock)
        OrderItemRequest item = new OrderItemRequest("bk_low_stock", 10, BigDecimal.valueOf(99.00));
        request.setItems(List.of(item));

        ShippingAddressRequest shipping = new ShippingAddressRequest();
        shipping.setFullName("Pankaj Kumar");
        shipping.setStreetAddress("456 Silicon Avenue");
        shipping.setCity("Bengaluru");
        shipping.setState("Karnataka");
        shipping.setPostalCode("560100");
        shipping.setCountry("IND");
        request.setShippingAddress(shipping);

        PaymentRequest payment = new PaymentRequest("CREDIT_CARD", "tok_test", "USD");
        request.setPayment(payment);

        mockMvc.perform(post("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.errorCode", is("ERR_INSUFFICIENT_STOCK")))
                .andExpect(jsonPath("$.title", is("Insufficient Inventory Conflict")))
                .andExpect(jsonPath("$.invalidItems", hasSize(1)))
                .andExpect(jsonPath("$.invalidItems[0].bookId", is("bk_low_stock")))
                .andExpect(jsonPath("$.invalidItems[0].requestedQuantity", is(10)));
    }

    @Test
    @DisplayName("POST /v1/orders - Failure Scenario: Validation Error with Missing Postal Code (422 Unprocessable Entity)")
    void testPlaceOrderValidationError() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId("usr_908214");

        // Missing postal code and missing valid item
        ShippingAddressRequest shipping = new ShippingAddressRequest();
        shipping.setFullName("Pankaj Kumar");
        shipping.setStreetAddress("456 Silicon Avenue");
        shipping.setCity("Bengaluru");
        shipping.setState("Karnataka");
        shipping.setPostalCode(""); // Empty postal code triggers validation error
        shipping.setCountry("IND");
        request.setShippingAddress(shipping);

        mockMvc.perform(post("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.errorCode", is("ERR_SCHEMA_VALIDATION")))
                .andExpect(jsonPath("$.fieldErrors", notNullValue()));
    }

    @Test
    @DisplayName("GET /v1/users/{userId}/orders - Retrieve All Orders of a User")
    void testGetUserOrders() throws Exception {
        mockMvc.perform(get("/v1/users/usr_908214/orders")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data", notNullValue()));
    }
}
