package com.rsl.training.subscription.dto;


import java.math.BigDecimal;

/**
 * Response record returning the calculated monthly subscription price details.
 *
 * @param tier         The applied subscription tier
 * @param tenureMonths The customer's tenure in months
 * @param voucher      The applied voucher code, if any
 * @param monthlyPrice The final rounded monthly price
 */
public record PricingResponse(
    SubscriptionTier tier,
    int tenureMonths,
    String voucher,
    BigDecimal monthlyPrice
) {}
