package com.rsl.training.subscription;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SubscriptionPricingService {

    private static final BigDecimal TEN_PERCENT_DISCOUNT = new BigDecimal("0.90");
    private static final BigDecimal TWENTY_FIVE_PERCENT_DISCOUNT = new BigDecimal("0.75");
    private static final BigDecimal HALF_PRICE_DISCOUNT = new BigDecimal("0.50");
    private static final BigDecimal SAVE20_DEDUCTION = new BigDecimal("20.00");

    public BigDecimal calculateMonthlyPrice(SubscriptionTier tier, int tenureMonths, String voucher) {
        if (tier == null) {
            throw new IllegalArgumentException("Subscription tier cannot be null.");
        }

        if (tenureMonths < 0) {
            throw new IllegalArgumentException("Tenure months cannot be negative: " + tenureMonths);
        }

        BigDecimal price = tier.getBasePrice();

        if (tenureMonths >= 13 && tenureMonths <= 36) {
            price = price.multiply(TEN_PERCENT_DISCOUNT);
        } else if (tenureMonths >= 37) {
            price = price.multiply(TWENTY_FIVE_PERCENT_DISCOUNT);
        }

        if (voucher != null && !voucher.isBlank()) {
            switch (voucher) {
                case "SAVE20":
                    price = price.subtract(SAVE20_DEDUCTION);
                    break;
                case "HALFPRICE":
                    price = price.multiply(HALF_PRICE_DISCOUNT);
                    break;
                default:
                    throw new InvalidVoucherException("Invalid voucher code: " + voucher);
            }
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            price = BigDecimal.ZERO;
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
