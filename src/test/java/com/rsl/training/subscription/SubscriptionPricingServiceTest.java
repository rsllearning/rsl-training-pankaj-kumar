package com.rsl.training.subscription;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SubscriptionPricingService Security and Functional Unit Test Suite")
class SubscriptionPricingServiceTest {

    private SubscriptionPricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new SubscriptionPricingService();
    }

    @Nested
    @DisplayName("1. Base Rates Validation")
    class BaseRatesTests {

        @ParameterizedTest(name = "Tier {0} should have base monthly rate of ${1}")
        @CsvSource({
            "BASIC, 50.00",
            "PRO, 150.00",
            "ENTERPRISE, 500.00"
        })
        @DisplayName("Verify exact monthly base price for all tiers (0 tenure, no voucher)")
        void shouldReturnCorrectBaseRateForZeroTenure(SubscriptionTier tier, String expectedPrice) {
            BigDecimal expected = new BigDecimal(expectedPrice);
            BigDecimal actual = pricingService.calculatePrice(tier, 0, null);

            assertNotNull(actual, "Calculated price must not be null");
            assertEquals(expected, actual, "Base rate must match standard tier specification");
            assertEquals(2, actual.scale(), "Monetary value must be represented with a scale of 2");
        }

        @ParameterizedTest(name = "Overload: Tier {0} base rate without voucher parameter should be ${1}")
        @CsvSource({
            "BASIC, 50.00",
            "PRO, 150.00",
            "ENTERPRISE, 500.00"
        })
        @DisplayName("Verify base rate using two-argument convenience overload")
        void shouldReturnCorrectBaseRateUsingTwoArgOverload(SubscriptionTier tier, String expectedPrice) {
            BigDecimal expected = new BigDecimal(expectedPrice);
            BigDecimal actual = pricingService.calculatePrice(tier, 0);

            assertNotNull(actual, "Calculated price must not be null");
            assertEquals(expected, actual, "Base rate must match standard tier specification");
            assertEquals(2, actual.scale(), "Monetary value must be represented with a scale of 2");
        }
    }

    @Nested
    @DisplayName("2. Longevity Discount Boundaries")
    class LongevityDiscountBoundariesTests {

        @ParameterizedTest(name = "{0} tier at boundary {1} months tenure -> Expected: ${2}")
        @CsvSource({
            // Boundary: 12 months (0% discount - upper bound of no discount)
            "BASIC, 12, 50.00",
            "PRO, 12, 150.00",
            "ENTERPRISE, 12, 500.00",

            // Boundary: 13 months (10% discount - lower bound of 10% discount)
            "BASIC, 13, 45.00",
            "PRO, 13, 135.00",
            "ENTERPRISE, 13, 450.00",

            // Boundary: 36 months (10% discount - upper bound of 10% discount)
            "BASIC, 36, 45.00",
            "PRO, 36, 135.00",
            "ENTERPRISE, 36, 450.00",

            // Boundary: 37 months (25% discount - lower bound of 25% discount)
            "BASIC, 37, 37.50",
            "PRO, 37, 112.50",
            "ENTERPRISE, 37, 375.00"
        })
        @DisplayName("Verify critical boundary tenure thresholds: 12, 13, 36, and 37 months")
        void shouldApplyCorrectLongevityDiscountAtExplicitBoundaries(SubscriptionTier tier, int tenureMonths, String expectedPrice) {
            BigDecimal actual = pricingService.calculatePrice(tier, tenureMonths, null);

            assertEquals(new BigDecimal(expectedPrice), actual);
            assertEquals(2, actual.scale(), "Monetary value must have a scale of 2");
        }

        @ParameterizedTest(name = "Tenure of {0} months for BASIC tier should receive 0% discount ($50.00)")
        @ValueSource(ints = {0, 1, 6, 11, 12})
        @DisplayName("Up to 12 months tenure: No discount (0%)")
        void shouldNotApplyDiscountForTenureUpToTwelveMonths(int months) {
            BigDecimal actual = pricingService.calculatePrice(SubscriptionTier.BASIC, months, null);
            assertEquals(new BigDecimal("50.00"), actual);
            assertEquals(2, actual.scale());
        }

        @ParameterizedTest(name = "Tenure of {0} months for BASIC tier should receive 10% discount ($45.00)")
        @ValueSource(ints = {13, 18, 24, 30, 36})
        @DisplayName("13 to 36 months tenure: 10% discount")
        void shouldApplyTenPercentDiscountBetweenThirteenAndThirtySixMonths(int months) {
            BigDecimal actual = pricingService.calculatePrice(SubscriptionTier.BASIC, months, null);
            assertEquals(new BigDecimal("45.00"), actual);
            assertEquals(2, actual.scale());
        }

        @ParameterizedTest(name = "Tenure of {0} months for BASIC tier should receive 25% discount ($37.50)")
        @ValueSource(ints = {37, 48, 60, 120})
        @DisplayName("More than 36 months (37+) tenure: 25% discount")
        void shouldApplyTwentyFivePercentDiscountForThirtySevenMonthsOrMore(int months) {
            BigDecimal actual = pricingService.calculatePrice(SubscriptionTier.BASIC, months, null);
            assertEquals(new BigDecimal("37.50"), actual);
            assertEquals(2, actual.scale());
        }
    }

    @Nested
    @DisplayName("3. Promotional Vouchers")
    class PromotionalVouchersTests {

        @ParameterizedTest(name = "{0} tier (0 tenure) with SAVE20 voucher -> Expected: ${1}")
        @CsvSource({
            "BASIC, 30.00",        // $50.00 - $20.00
            "PRO, 130.00",         // $150.00 - $20.00
            "ENTERPRISE, 480.00"   // $500.00 - $20.00
        })
        @DisplayName("SAVE20 deducts flat $20.00 fee from base price")
        void shouldDeductTwentyDollarsWithSave20VoucherOnBasePrice(SubscriptionTier tier, String expectedPrice) {
            BigDecimal actual = pricingService.calculatePrice(tier, 0, "SAVE20");
            assertEquals(new BigDecimal(expectedPrice), actual);
            assertEquals(2, actual.scale());
        }

        @ParameterizedTest(name = "{0} tier at {1} months tenure with SAVE20 -> Expected: ${2}")
        @CsvSource({
            "BASIC, 13, 25.00",        // ($50.00 - 10%) - $20.00 = $45.00 - $20.00 = $25.00
            "BASIC, 37, 17.50",        // ($50.00 - 25%) - $20.00 = $37.50 - $20.00 = $17.50
            "PRO, 13, 115.00",         // ($150.00 - 10%) - $20.00 = $135.00 - $20.00 = $115.00
            "PRO, 37, 92.50",          // ($150.00 - 25%) - $20.00 = $112.50 - $20.00 = $92.50
            "ENTERPRISE, 13, 430.00",  // ($500.00 - 10%) - $20.00 = $450.00 - $20.00 = $430.00
            "ENTERPRISE, 37, 355.00"   // ($500.00 - 25%) - $20.00 = $375.00 - $20.00 = $355.00
        })
        @DisplayName("SAVE20 deducts $20.00 strictly AFTER longevity percentage discount is applied")
        void shouldDeductTwentyDollarsAfterLongevityDiscountApplied(SubscriptionTier tier, int tenureMonths, String expectedPrice) {
            BigDecimal actual = pricingService.calculatePrice(tier, tenureMonths, "SAVE20");
            assertEquals(new BigDecimal(expectedPrice), actual);
            assertEquals(2, actual.scale());
        }

        @ParameterizedTest(name = "{0} tier (0 tenure) with HALFPRICE voucher -> Expected: ${1}")
        @CsvSource({
            "BASIC, 25.00",        // $50.00 * 50%
            "PRO, 75.00",          // $150.00 * 50%
            "ENTERPRISE, 250.00"   // $500.00 * 50%
        })
        @DisplayName("HALFPRICE reduces base price by 50%")
        void shouldReducePriceByHalfWithHalfPriceVoucherOnBasePrice(SubscriptionTier tier, String expectedPrice) {
            BigDecimal actual = pricingService.calculatePrice(tier, 0, "HALFPRICE");
            assertEquals(new BigDecimal(expectedPrice), actual);
            assertEquals(2, actual.scale());
        }

        @ParameterizedTest(name = "{0} tier at {1} months tenure with HALFPRICE -> Expected: ${2}")
        @CsvSource({
            "BASIC, 13, 22.50",        // ($50.00 - 10%) * 50% = $45.00 * 0.50 = $22.50
            "BASIC, 37, 18.75",        // ($50.00 - 25%) * 50% = $37.50 * 0.50 = $18.75
            "PRO, 13, 67.50",          // ($150.00 - 10%) * 50% = $135.00 * 0.50 = $67.50
            "PRO, 37, 56.25",          // ($150.00 - 25%) * 50% = $112.50 * 0.50 = $56.25
            "ENTERPRISE, 13, 225.00",  // ($500.00 - 10%) * 50% = $450.00 * 0.50 = $225.00
            "ENTERPRISE, 37, 187.50"   // ($500.00 - 25%) * 50% = $375.00 * 0.50 = $187.50
        })
        @DisplayName("HALFPRICE reduces longevity-adjusted price by 50%")
        void shouldReducePriceByHalfAfterLongevityDiscountApplied(SubscriptionTier tier, int tenureMonths, String expectedPrice) {
            BigDecimal actual = pricingService.calculatePrice(tier, tenureMonths, "HALFPRICE");
            assertEquals(new BigDecimal(expectedPrice), actual);
            assertEquals(2, actual.scale());
        }

        @ParameterizedTest(name = "Unrecognized/invalid voucher code ''{0}'' must throw InvalidVoucherException")
        @ValueSource(strings = {
            "INVALID",
            "EXPIRED2024",
            "SAVE10",
            "DISCOUNT50",
            "save20",                    // Case-sensitive check
            "Save20",                    // Case-sensitive check
            "halfprice",                 // Case-sensitive check
            "HalfPrice",                 // Case-sensitive check
            " SAVE20",                   // Leading whitespace
            "SAVE20 ",                   // Trailing whitespace
            "SAVE 20",                   // Embedded whitespace
            "' OR '1'='1",               // SQL injection attack vector
            "<script>alert(1)</script>", // XSS attack vector
            "../../etc/passwd",          // Path traversal vector
            "SAVE20\u0000INJECT"         // Null-byte injection vector
        })
        @DisplayName("Security Audit: Unrecognized, invalid, expired, or malicious voucher codes must throw InvalidVoucherException")
        void shouldThrowInvalidVoucherExceptionForInvalidOrUnrecognizedVouchers(String invalidVoucher) {
            assertThrows(InvalidVoucherException.class, () ->
                pricingService.calculatePrice(SubscriptionTier.BASIC, 12, invalidVoucher)
            );
        }
    }

    @Nested
    @DisplayName("4. Financial Rules & Boundaries")
    class FinancialRulesAndBoundariesTests {

        @Test
        @DisplayName("Price floor is strictly $0.00: Clamps to $0.00 when deductions exceed total price")
        void shouldClampPriceToZeroWhenDeductionExceedsTotal() {
            // Verifies the non-negotiable financial security rule: final total can never be negative (< $0.00)
            BigDecimal actual = pricingService.calculatePrice(SubscriptionTier.BASIC, 37, "SAVE20");
            assertTrue(actual.compareTo(BigDecimal.ZERO) >= 0, "Price must never drop below $0.00 floor");
            assertEquals(2, actual.scale(), "Floor value must maintain scale of 2");
        }

        @Test
        @DisplayName("HALF_UP Rounding: Fractional cents must round to two decimal places using HALF_UP")
        void shouldRoundToTwoDecimalPlacesUsingHalfUp() {
            // BASIC ($50.00) at 37 months has 25% longevity discount = $37.50
            // HALFPRICE applied to $37.50 yields exactly $18.75
            BigDecimal actual = pricingService.calculatePrice(SubscriptionTier.BASIC, 37, "HALFPRICE");
            assertEquals(new BigDecimal("18.75"), actual);
            assertEquals(2, actual.scale(), "Scale must be strictly 2 decimal places");

            // PRO ($150.00) at 37 months has 25% longevity discount = $112.50
            // HALFPRICE applied to $112.50 yields exactly $56.25
            BigDecimal proActual = pricingService.calculatePrice(SubscriptionTier.PRO, 37, "HALFPRICE");
            assertEquals(new BigDecimal("56.25"), proActual);
            assertEquals(2, proActual.scale(), "Scale must be strictly 2 decimal places");
        }

        @ParameterizedTest(name = "Result scale for tier {0} at tenure {1} months with voucher {2} must be 2")
        @CsvSource({
            "BASIC, 0, null",
            "BASIC, 13, null",
            "BASIC, 37, null",
            "BASIC, 13, SAVE20",
            "BASIC, 13, HALFPRICE",
            "PRO, 37, HALFPRICE",
            "ENTERPRISE, 37, SAVE20"
        })
        @DisplayName("Financial Precision: All computed monthly totals must strictly enforce a scale of 2")
        void shouldEnforceStrictTwoDecimalPlacesScaleAcrossCalculations(SubscriptionTier tier, int tenureMonths, String voucher) {
            String sanitizedVoucher = "null".equalsIgnoreCase(voucher) ? null : voucher;
            BigDecimal actual = pricingService.calculatePrice(tier, tenureMonths, sanitizedVoucher);
            assertEquals(2, actual.scale(), "Calculated currency scale must always be 2 decimal places");
        }
    }

    @Nested
    @DisplayName("5. Edge Cases & Validations")
    class EdgeCasesAndValidationTests {

        @Test
        @DisplayName("Input Validation: Null tier must throw IllegalArgumentException")
        void shouldThrowIllegalArgumentExceptionWhenTierIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                pricingService.calculatePrice(null, 12, null)
            );
            assertThrows(IllegalArgumentException.class, () ->
                pricingService.calculatePrice(null, 0, "SAVE20")
            );
            assertThrows(IllegalArgumentException.class, () ->
                pricingService.calculatePrice(null, 36)
            );
        }

        @ParameterizedTest(name = "Negative tenure of {0} months must throw IllegalArgumentException")
        @ValueSource(ints = {-1, -2, -12, -36, -100, Integer.MIN_VALUE})
        @DisplayName("Input Validation: Negative tenure months must throw IllegalArgumentException")
        void shouldThrowIllegalArgumentExceptionWhenTenureIsNegative(int negativeTenure) {
            assertThrows(IllegalArgumentException.class, () ->
                pricingService.calculatePrice(SubscriptionTier.BASIC, negativeTenure, null)
            );
            assertThrows(IllegalArgumentException.class, () ->
                pricingService.calculatePrice(SubscriptionTier.PRO, negativeTenure, "SAVE20")
            );
            assertThrows(IllegalArgumentException.class, () ->
                pricingService.calculatePrice(SubscriptionTier.ENTERPRISE, negativeTenure)
            );
        }

        @ParameterizedTest(name = "Voucher ''{0}'' should be handled gracefully as no discount applied")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n", " \t \n "})
        @DisplayName("Input Validation: Null, empty, or blank vouchers must be handled gracefully with no discount")
        void shouldHandleNullEmptyOrBlankVouchersGracefully(String noDiscountVoucher) {
            // For BASIC with 0 tenure months, base price is $50.00
            BigDecimal actual = pricingService.calculatePrice(SubscriptionTier.BASIC, 0, noDiscountVoucher);
            assertEquals(new BigDecimal("50.00"), actual);
            assertEquals(2, actual.scale());

            // For PRO with 13 tenure months (10% longevity), price is $135.00
            BigDecimal actualPro = pricingService.calculatePrice(SubscriptionTier.PRO, 13, noDiscountVoucher);
            assertEquals(new BigDecimal("135.00"), actualPro);
            assertEquals(2, actualPro.scale());
        }

        @Test
        @DisplayName("Boundary Validation: Tenure of exactly 0 months is valid (new subscriber)")
        void shouldProcessZeroMonthsTenureSuccessfully() {
            BigDecimal actual = pricingService.calculatePrice(SubscriptionTier.BASIC, 0, null);
            assertEquals(new BigDecimal("50.00"), actual);
            assertEquals(2, actual.scale());
        }

        @Test
        @DisplayName("Boundary Validation: Extreme tenure months (Integer.MAX_VALUE) must not overflow")
        void shouldHandleExtremeLongevityTenureWithoutOverflow() {
            BigDecimal actual = pricingService.calculatePrice(SubscriptionTier.ENTERPRISE, Integer.MAX_VALUE, null);
            // 25% longevity discount on $500.00 is $375.00
            assertEquals(new BigDecimal("375.00"), actual);
            assertEquals(2, actual.scale());
        }
    }
}
