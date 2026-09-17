package com.rsl.training.subscription;

import java.math.BigDecimal;

public enum SubscriptionTier {
    BASIC(new BigDecimal("50.00")),
    PRO(new BigDecimal("150.00")),
    ENTERPRISE(new BigDecimal("500.00"));

    private final BigDecimal basePrice;

    SubscriptionTier(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }
}
