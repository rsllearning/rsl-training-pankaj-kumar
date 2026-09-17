package com.rsl.training.subscription.controller;

import com.rsl.training.subscription.SubscriptionPricingApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SubscriptionPricingApplication.class)
@AutoConfigureMockMvc
@DisplayName("SubscriptionPricingController Integration Tests")
class SubscriptionPricingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /calculate returns 200 and base price for BASIC tier with 0 tenure")
    void shouldCalculateBasePriceForBasicTier() throws Exception {
        String requestBody = """
            {
                "tier": "BASIC",
                "tenureMonths": 0
            }
            """;

        mockMvc.perform(post("/api/subscriptions/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tier", is("BASIC")))
            .andExpect(jsonPath("$.tenureMonths", is(0)))
            .andExpect(jsonPath("$.monthlyPrice", is(50.00)));
    }

    @Test
    @DisplayName("POST /calculate returns 200 with 10% discount for PRO tier at 13 months tenure")
    void shouldCalculateLongevityDiscountForProTier() throws Exception {
        String requestBody = """
            {
                "tier": "PRO",
                "tenureMonths": 13
            }
            """;

        mockMvc.perform(post("/api/subscriptions/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tier", is("PRO")))
            .andExpect(jsonPath("$.tenureMonths", is(13)))
            .andExpect(jsonPath("$.monthlyPrice", is(135.00)));
    }

    @Test
    @DisplayName("POST /calculate returns 200 applying longevity discount and SAVE20 voucher correctly")
    void shouldApplyLongevityAndSave20Voucher() throws Exception {
        String requestBody = """
            {
                "tier": "ENTERPRISE",
                "tenureMonths": 37,
                "voucher": "SAVE20"
            }
            """;

        mockMvc.perform(post("/api/subscriptions/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tier", is("ENTERPRISE")))
            .andExpect(jsonPath("$.tenureMonths", is(37)))
            .andExpect(jsonPath("$.voucher", is("SAVE20")))
            .andExpect(jsonPath("$.monthlyPrice", is(355.00)));
    }

    @Test
    @DisplayName("POST /calculate returns 200 with HALFPRICE voucher")
    void shouldApplyHalfPriceVoucher() throws Exception {
        String requestBody = """
            {
                "tier": "BASIC",
                "tenureMonths": 0,
                "voucher": "HALFPRICE"
            }
            """;

        mockMvc.perform(post("/api/subscriptions/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tier", is("BASIC")))
            .andExpect(jsonPath("$.monthlyPrice", is(25.00)));
    }

    @Test
    @DisplayName("POST /calculate returns 400 Bad Request when an invalid voucher code is supplied")
    void shouldReturnBadRequestForInvalidVoucher() throws Exception {
        String requestBody = """
            {
                "tier": "BASIC",
                "tenureMonths": 6,
                "voucher": "BOGUS_CODE"
            }
            """;

        mockMvc.perform(post("/api/subscriptions/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.error", is("Bad Request")))
            .andExpect(jsonPath("$.message", is("Invalid voucher code: BOGUS_CODE")));
    }

    @Test
    @DisplayName("POST /calculate returns 400 Bad Request when tier is null")
    void shouldReturnBadRequestWhenTierIsNull() throws Exception {
        String requestBody = """
            {
                "tier": null,
                "tenureMonths": 10
            }
            """;

        mockMvc.perform(post("/api/subscriptions/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.message", is("Subscription tier cannot be null.")));
    }

    @Test
    @DisplayName("POST /calculate returns 400 Bad Request when tenure is negative")
    void shouldReturnBadRequestWhenTenureIsNegative() throws Exception {
        String requestBody = """
            {
                "tier": "BASIC",
                "tenureMonths": -5
            }
            """;

        mockMvc.perform(post("/api/subscriptions/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.message", is("Tenure months cannot be negative: -5")));
    }

    @Test
    @DisplayName("POST /calculate returns 400 Bad Request for malformed JSON payload")
    void shouldReturnBadRequestForMalformedPayload() throws Exception {
        String requestBody = """
            {
                "tier": "NON_EXISTENT_TIER"
            }
            """;

        mockMvc.perform(post("/api/subscriptions/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)));
    }
}
