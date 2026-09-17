package com.rsl.training.subscription.service;

import com.rsl.training.subscription.dto.SubscriptionTier;
import com.rsl.training.subscription.exception.InvalidVoucherException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service responsible for calculating subscription pricing based on tier,
 * tenure longevity discounts, and promotional vouchers using modern Java 21 logic.
 */
@Service
public class SubscriptionPricingService {

    private static final BigDecimal TEN_PERCENT_DISCOUNT = new BigDecimal("0.90");
    private static final BigDecimal TWENTY_FIVE_PERCENT_DISCOUNT = new BigDecimal("0.75");
    private static final BigDecimal HALF_PRICE_DISCOUNT = new BigDecimal("0.50");
    private static final BigDecimal SAVE20_DEDUCTION = new BigDecimal("20.00");

    private enum LongevityTier {
        NONE,       // 0 - 12 months (0% discount)
        STANDARD,   // 13 - 36 months (10% discount)
        LOYAL       // 37+ months (25% discount)
    }

    /**
     * Calculates the final monthly subscription price after longevity discounts and voucher deductions.
     *
     * @param tier         The subscription tier (cannot be null)
     * @param tenureMonths Customer tenure in months (must be non-negative)
     * @param voucher      Optional promotional voucher code
     * @return The final monthly price rounded to 2 decimal places with HALF_UP rounding
     * @throws IllegalArgumentException if tier is null or tenureMonths is negative
     * @throws InvalidVoucherException  if an unrecognized voucher is supplied
     */
    public BigDecimal calculateMonthlyPrice(SubscriptionTier tier, int tenureMonths, String voucher) {
        validateInputs(tier, tenureMonths);

        BigDecimal basePrice = tier.getBasePrice();
        BigDecimal priceAfterLongevity = applyLongevityDiscount(basePrice, tenureMonths);
        BigDecimal priceAfterVoucher = applyVoucherDiscount(priceAfterLongevity, voucher);

        return clampToFloorAndScale(priceAfterVoucher);
    }

    private void validateInputs(SubscriptionTier tier, int tenureMonths) {
        if (tier == null) {
            throw new IllegalArgumentException("Subscription tier cannot be null.");
        }
        if (tenureMonths < 0) {
            throw new IllegalArgumentException("Tenure months cannot be negative: " + tenureMonths);
        }
    }

    private LongevityTier determineLongevityTier(int tenureMonths) {
        if (tenureMonths <= 12) {
            return LongevityTier.NONE;
        } else if (tenureMonths <= 36) {
            return LongevityTier.STANDARD;
        } else {
            return LongevityTier.LOYAL;
        }
    }

    private BigDecimal applyLongevityDiscount(BigDecimal basePrice, int tenureMonths) {
        BigDecimal discountFactor = switch (determineLongevityTier(tenureMonths)) {
            case NONE -> BigDecimal.ONE;
            case STANDARD -> TEN_PERCENT_DISCOUNT;
            case LOYAL -> TWENTY_FIVE_PERCENT_DISCOUNT;
        };

        return basePrice.multiply(discountFactor);
    }

    private BigDecimal applyVoucherDiscount(BigDecimal price, String voucher) {
        if (voucher == null || voucher.isBlank()) {
            return price;
        }

        return switch (voucher) {
            case "SAVE20" -> price.subtract(SAVE20_DEDUCTION);
            case "HALFPRICE" -> price.multiply(HALF_PRICE_DISCOUNT);
            default -> throw new InvalidVoucherException("Invalid voucher code: " + voucher);
        };
    }

    private BigDecimal clampToFloorAndScale(BigDecimal price) {
        return price.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
