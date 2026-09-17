package com.rsl.training.subscription.dto;


/**
 * Request record representing inputs required to calculate subscription pricing.
 *
 * @param tier         The subscription tier (BASIC, PRO, ENTERPRISE)
 * @param tenureMonths Customer tenure in months (defaults to 0 if omitted)
 * @param voucher      Optional promotional voucher code (e.g., SAVE20, HALFPRICE)
 */
public record PricingRequest(
    SubscriptionTier tier,
    Integer tenureMonths,
    String voucher
) {
    public int effectiveTenureMonths() {
        return tenureMonths != null ? tenureMonths : 0;
    }
}
