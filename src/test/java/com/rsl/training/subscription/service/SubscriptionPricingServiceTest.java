package com.rsl.training.subscription.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.rsl.training.subscription.dto.SubscriptionTier;
import com.rsl.training.subscription.exception.InvalidVoucherException;
import com.rsl.training.subscription.service.SubscriptionPricingService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SubscriptionPricingService Functional and Security Unit Test Suite")
class SubscriptionPricingServiceTest {

    private SubscriptionPricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new SubscriptionPricingService();
    }

    @Nested
    @DisplayName("Base Rates and Tier Validation")
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

            BigDecimal actual = pricingService.calculateMonthlyPrice(tier, 0, null);

            assertEquals(expected, actual, "Base rate must match standard tier specification");
            assertEquals(2, actual.scale(), "Monetary value must be represented with a scale of 2");
        }

        @Test
        @DisplayName("Input Validation: Null tier must throw IllegalArgumentException with descriptive message")
        void shouldThrowIllegalArgumentExceptionWithDescriptiveMessageWhenTierIsNull() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                pricingService.calculateMonthlyPrice(null, 12, null)
            );

            assertNotNull(exception.getMessage(), "Exception message must not be null");
            assertTrue(exception.getMessage().toLowerCase().contains("tier"),
                "Exception message must specify that tier cannot be null");
        }
    }

    @Nested
    @DisplayName("Longevity Discount Boundaries and Tenure Validation")
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
            BigDecimal actual = pricingService.calculateMonthlyPrice(tier, tenureMonths, null);

            assertEquals(new BigDecimal(expectedPrice), actual, "Discounted price must match exact expected boundary price");
            assertEquals(2, actual.scale(), "Monetary value must have a scale of 2");
        }

        @ParameterizedTest(name = "Interior tenure of {0} months for BASIC tier should receive 0% discount ($50.00)")
        @ValueSource(ints = {1, 6, 11})
        @DisplayName("Interior values up to 12 months tenure: No discount (0%)")
        void shouldNotApplyDiscountForInteriorTenureUpToTwelveMonths(int months) {
            BigDecimal actual = pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, months, null);

            assertEquals(new BigDecimal("50.00"), actual);
            assertEquals(2, actual.scale());
        }

        @ParameterizedTest(name = "Interior tenure of {0} months for BASIC tier should receive 10% discount ($45.00)")
        @ValueSource(ints = {18, 24, 30})
        @DisplayName("Interior values between 13 and 36 months tenure: 10% discount")
        void shouldApplyTenPercentDiscountForInteriorTenureBetweenThirteenAndThirtySixMonths(int months) {
            BigDecimal actual = pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, months, null);

            assertEquals(new BigDecimal("45.00"), actual);
            assertEquals(2, actual.scale());
        }

        @ParameterizedTest(name = "Interior tenure of {0} months for BASIC tier should receive 25% discount ($37.50)")
        @ValueSource(ints = {48, 60, 120})
        @DisplayName("Interior values above 36 months tenure: 25% discount")
        void shouldApplyTwentyFivePercentDiscountForInteriorTenureAboveThirtySixMonths(int months) {
            BigDecimal actual = pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, months, null);

            assertEquals(new BigDecimal("37.50"), actual);
            assertEquals(2, actual.scale());
        }

        @ParameterizedTest(name = "Negative tenure of {0} months must throw IllegalArgumentException")
        @ValueSource(ints = {-1, -2, -12, -36, -100, Integer.MIN_VALUE})
        @DisplayName("Input Validation: Negative tenure months must throw IllegalArgumentException with descriptive message")
        void shouldThrowIllegalArgumentExceptionWithDescriptiveMessageWhenTenureIsNegative(int negativeTenure) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, negativeTenure, null)
            );

            assertNotNull(exception.getMessage(), "Exception message must not be null");
            assertTrue(exception.getMessage().toLowerCase().contains("tenure"),
                "Exception message must specify that tenure cannot be negative");
        }

        @Test
        @DisplayName("Boundary Validation: Extreme tenure months (Integer.MAX_VALUE) must not overflow")
        void shouldHandleExtremeLongevityTenureWithoutOverflow() {
            BigDecimal actual = pricingService.calculateMonthlyPrice(SubscriptionTier.ENTERPRISE, Integer.MAX_VALUE, null);

            assertEquals(new BigDecimal("375.00"), actual);
            assertEquals(2, actual.scale());
        }
    }

    @Nested
    @DisplayName("Promotional Vouchers and Validation")
    class PromotionalVouchersTests {

        @ParameterizedTest(name = "{0} tier (0 tenure) with SAVE20 voucher -> Expected: ${1}")
        @CsvSource({
            "BASIC, 30.00",        // $50.00 - $20.00
            "PRO, 130.00",         // $150.00 - $20.00
            "ENTERPRISE, 480.00"   // $500.00 - $20.00
        })
        @DisplayName("SAVE20 deducts flat $20.00 fee from base price")
        void shouldDeductTwentyDollarsWithSave20VoucherOnBasePrice(SubscriptionTier tier, String expectedPrice) {
            BigDecimal actual = pricingService.calculateMonthlyPrice(tier, 0, "SAVE20");

            assertEquals(new BigDecimal(expectedPrice), actual);
            assertEquals(2, actual.scale());
        }

        @ParameterizedTest(name = "{0} tier at {1} months tenure with SAVE20 -> Expected: ${2}")
        @CsvSource({
            // Boundary: 12 months (0% longevity discount) - $20 flat deduction
            "BASIC, 12, 30.00",        // ($50.00 - 0%) - $20.00 = $30.00
            "PRO, 12, 130.00",         // ($150.00 - 0%) - $20.00 = $130.00
            "ENTERPRISE, 12, 480.00",  // ($500.00 - 0%) - $20.00 = $480.00

            // Boundary: 13 months (10% longevity discount) - $20 flat deduction
            "BASIC, 13, 25.00",        // ($50.00 - 10%) - $20.00 = $45.00 - $20.00 = $25.00
            "PRO, 13, 115.00",         // ($150.00 - 10%) - $20.00 = $135.00 - $20.00 = $115.00
            "ENTERPRISE, 13, 430.00",  // ($500.00 - 10%) - $20.00 = $450.00 - $20.00 = $430.00

            // Boundary: 36 months (10% longevity discount) - $20 flat deduction
            "BASIC, 36, 25.00",        // ($50.00 - 10%) - $20.00 = $45.00 - $20.00 = $25.00
            "PRO, 36, 115.00",         // ($150.00 - 10%) - $20.00 = $135.00 - $20.00 = $115.00
            "ENTERPRISE, 36, 430.00",  // ($500.00 - 10%) - $20.00 = $450.00 - $20.00 = $430.00

            // Boundary: 37 months (25% longevity discount) - $20 flat deduction
            "BASIC, 37, 17.50",        // ($50.00 - 25%) - $20.00 = $37.50 - $20.00 = $17.50
            "PRO, 37, 92.50",          // ($150.00 - 25%) - $20.00 = $112.50 - $20.00 = $92.50
            "ENTERPRISE, 37, 355.00"   // ($500.00 - 25%) - $20.00 = $375.00 - $20.00 = $355.00
        })
        @DisplayName("SAVE20 deducts $20.00 strictly AFTER longevity percentage discount is applied across all boundaries")
        void shouldDeductTwentyDollarsAfterLongevityDiscountApplied(SubscriptionTier tier, int tenureMonths, String expectedPrice) {
            BigDecimal actual = pricingService.calculateMonthlyPrice(tier, tenureMonths, "SAVE20");

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
            BigDecimal actual = pricingService.calculateMonthlyPrice(tier, 0, "HALFPRICE");

            assertEquals(new BigDecimal(expectedPrice), actual);
            assertEquals(2, actual.scale());
        }

        @ParameterizedTest(name = "{0} tier at {1} months tenure with HALFPRICE -> Expected: ${2}")
        @CsvSource({
            // Boundary: 12 months (0% longevity discount) - 50% voucher discount
            "BASIC, 12, 25.00",        // ($50.00 - 0%) * 50% = $25.00
            "PRO, 12, 75.00",          // ($150.00 - 0%) * 50% = $75.00
            "ENTERPRISE, 12, 250.00",  // ($500.00 - 0%) * 50% = $250.00

            // Boundary: 13 months (10% longevity discount) - 50% voucher discount
            "BASIC, 13, 22.50",        // ($50.00 - 10%) * 50% = $45.00 * 0.50 = $22.50
            "PRO, 13, 67.50",          // ($150.00 - 10%) * 50% = $135.00 * 0.50 = $67.50
            "ENTERPRISE, 13, 225.00",  // ($500.00 - 10%) * 50% = $450.00 * 0.50 = $225.00

            // Boundary: 36 months (10% longevity discount) - 50% voucher discount
            "BASIC, 36, 22.50",        // ($50.00 - 10%) * 50% = $45.00 * 0.50 = $22.50
            "PRO, 36, 67.50",          // ($150.00 - 10%) * 50% = $135.00 * 0.50 = $67.50
            "ENTERPRISE, 36, 225.00",  // ($500.00 - 10%) * 50% = $450.00 * 0.50 = $225.00

            // Boundary: 37 months (25% longevity discount) - 50% voucher discount
            "BASIC, 37, 18.75",        // ($50.00 - 25%) * 50% = $37.50 * 0.50 = $18.75
            "PRO, 37, 56.25",          // ($150.00 - 25%) * 50% = $112.50 * 0.50 = $56.25
            "ENTERPRISE, 37, 187.50"   // ($500.00 - 25%) * 50% = $375.00 * 0.50 = $187.50
        })
        @DisplayName("HALFPRICE reduces longevity-adjusted price by 50% across all boundaries")
        void shouldReducePriceByHalfAfterLongevityDiscountApplied(SubscriptionTier tier, int tenureMonths, String expectedPrice) {
            BigDecimal actual = pricingService.calculateMonthlyPrice(tier, tenureMonths, "HALFPRICE");

            assertEquals(new BigDecimal(expectedPrice), actual);
            assertEquals(2, actual.scale());
        }

        @ParameterizedTest(name = "Blank voucher ''{0}'' on BASIC tier at 0 months yields base $50.00")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n", " \t \n "})
        @DisplayName("Input Validation: Null, empty, or blank vouchers must be handled gracefully with no discount applied")
        void shouldHandleNullEmptyOrBlankVouchersGracefullyOnBasePrice(String noDiscountVoucher) {
            BigDecimal actual = pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, 0, noDiscountVoucher);

            assertEquals(new BigDecimal("50.00"), actual);
            assertEquals(2, actual.scale());
        }

        @Test
        @DisplayName("Input Validation: Blank voucher does not alter existing longevity discounts")
        void shouldNotAlterLongevityDiscountWhenVoucherIsBlank() {
            BigDecimal actual = pricingService.calculateMonthlyPrice(SubscriptionTier.PRO, 13, "   ");

            assertEquals(new BigDecimal("135.00"), actual);
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
            "\tSAVE20",                  // Leading tab
            "SAVE20\n",                  // Trailing newline
            "SAVE-20",                   // Hyphenated variant
            "UNKNOWN_PROMO",             // Unknown promo code
            "SAVE20,HALFPRICE",          // Voucher stacking attempt
            "SAVE20+SAVE20",             // Voucher repetition attempt
            "SАVE20",                    // Unicode homoglyph: Cyrillic 'А' (U+0410) instead of Latin 'A'
            "\uFEFFSAVE20",              // Byte Order Mark (BOM) / zero-width no-break space prefix
            "SAVE20\u200B"               // Zero-width space suffix (U+200B)
        })
        @DisplayName("Security & Contract: Unrecognized or invalid voucher codes must throw InvalidVoucherException referencing the offending voucher")
        void shouldThrowInvalidVoucherExceptionWithDescriptiveMessageForInvalidVoucher(String invalidVoucher) {
            InvalidVoucherException exception = assertThrows(InvalidVoucherException.class, () ->
                pricingService.calculateMonthlyPrice(SubscriptionTier.BASIC, 12, invalidVoucher)
            );

            assertNotNull(exception.getMessage(), "Exception message must not be null");
            assertTrue(exception.getMessage().contains(invalidVoucher),
                "Exception message must explicitly reference the invalid voucher code");
        }
    }
}
