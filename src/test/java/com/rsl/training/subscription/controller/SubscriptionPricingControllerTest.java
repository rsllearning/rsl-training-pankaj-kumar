package com.rsl.training.subscription.controller;

import com.rsl.training.subscription.SubscriptionPricingApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest(name = "Invalid voucher ''{0}'' returns 400 Bad Request")
    @ValueSource(strings = {
        "BOGUS_CODE",   // Unrecognized voucher code
        "save20",       // Case-sensitive lowercase
        "halfprice",    // Case-sensitive lowercase
        " SAVE20 ",     // Leading and trailing whitespace
        "SАVE20",       // Cyrillic homoglyph: 'А' (U+0410)
        "SAVE20\u200B", // Zero-width space
        "\uFEFFSAVE20"  // Byte Order Mark (BOM)
    })
    @DisplayName("POST /calculate returns 400 Bad Request with sanitized error structure for invalid and homoglyph vouchers")
    void shouldRejectInvalidAndHomoglyphVouchersWithBadRequest(String invalidVoucher) throws Exception {
        String requestBody = String.format("""
            {
                "tier": "BASIC",
                "tenureMonths": 6,
                "voucher": "%s"
            }
            """, invalidVoucher);

        mockMvc.perform(post("/api/subscriptions/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.error", is("Bad Request")))
            .andExpect(jsonPath("$.message", is("Invalid voucher code: " + invalidVoucher)));
    }

    @ParameterizedTest(name = "POST /calculate boundary: {0} at {1}m with voucher ''{2}'' -> ${3}")
    @CsvSource({
        // 12m vs 13m threshold (0% to 10%)
        "BASIC, 12, '', 50.00",
        "BASIC, 13, '', 45.00",
        "ENTERPRISE, 12, '', 500.00",
        "ENTERPRISE, 13, '', 450.00",

        // 36m vs 37m threshold (10% to 25%)
        "BASIC, 36, '', 45.00",
        "BASIC, 37, '', 37.50",
        "PRO, 36, '', 135.00",
        "PRO, 37, '', 112.50",

        // Boundary transitions under SAVE20
        "BASIC, 12, SAVE20, 30.00",
        "BASIC, 13, SAVE20, 25.00",
        "PRO, 12, SAVE20, 130.00",
        "PRO, 13, SAVE20, 115.00",
        "BASIC, 36, SAVE20, 25.00",
        "BASIC, 37, SAVE20, 17.50",
        "PRO, 36, SAVE20, 115.00",
        "PRO, 37, SAVE20, 92.50",

        // Boundary transitions under HALFPRICE
        "BASIC, 12, HALFPRICE, 25.00",
        "BASIC, 13, HALFPRICE, 22.50",
        "BASIC, 36, HALFPRICE, 22.50",
        "BASIC, 37, HALFPRICE, 18.75",
        "ENTERPRISE, 36, HALFPRICE, 225.00",
        "ENTERPRISE, 37, HALFPRICE, 187.50"
    })
    @DisplayName("POST /calculate returns 200 with accurate boundary prices at 12/13 and 36/37 month thresholds")
    void shouldCalculateBoundaryTenureAccuratelyThroughRestEndpoint(String tier, int tenureMonths, String voucher, double expectedPrice) throws Exception {
        String voucherField = voucher.isEmpty() ? "" : String.format(", \"voucher\": \"%s\"", voucher);
        String requestBody = String.format("""
            {
                "tier": "%s",
                "tenureMonths": %d%s
            }
            """, tier, tenureMonths, voucherField);

        mockMvc.perform(post("/api/subscriptions/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tier", is(tier)))
            .andExpect(jsonPath("$.tenureMonths", is(tenureMonths)))
            .andExpect(jsonPath("$.monthlyPrice", is(expectedPrice)));
    }

    @ParameterizedTest(name = "POST /calculate handles omitted/null tenure: {0} -> Expected ${3}")
    @CsvSource(delimiter = '|', value = {
        // Completely omitted tenure
        "{\"tier\": \"BASIC\"} | BASIC | 0 | 50.00",
        // Explicit null tenure
        "{\"tier\": \"PRO\", \"tenureMonths\": null} | PRO | 0 | 150.00",
        // Omitted tenure with SAVE20
        "{\"tier\": \"ENTERPRISE\", \"voucher\": \"SAVE20\"} | ENTERPRISE | 0 | 480.00",
        // Null tenure with HALFPRICE
        "{\"tier\": \"PRO\", \"tenureMonths\": null, \"voucher\": \"HALFPRICE\"} | PRO | 0 | 75.00"
    })
    @DisplayName("POST /calculate gracefully defaults omitted or null tenure to 0 and calculates base pricing")
    void shouldHandleOmittedAndNullTenureGracefully(String jsonPayload, String expectedTier, int expectedTenure, double expectedPrice) throws Exception {
        mockMvc.perform(post("/api/subscriptions/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tier", is(expectedTier)))
            .andExpect(jsonPath("$.tenureMonths", is(expectedTenure)))
            .andExpect(jsonPath("$.monthlyPrice", is(expectedPrice)));
    }
}
