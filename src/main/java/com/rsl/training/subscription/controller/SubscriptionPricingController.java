package com.rsl.training.subscription.controller;

import com.rsl.training.subscription.dto.SubscriptionTier;
import com.rsl.training.subscription.dto.PricingRequest;
import com.rsl.training.subscription.dto.PricingResponse;
import com.rsl.training.subscription.service.SubscriptionPricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * REST controller providing endpoints to calculate subscription pricing.
 */
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionPricingController {

    private final SubscriptionPricingService pricingService;

    public SubscriptionPricingController(SubscriptionPricingService pricingService) {
        this.pricingService = pricingService;
    }

    /**
     * Calculates monthly subscription price via POST request.
     *
     * @param request JSON body containing tier, tenureMonths, and optional voucher
     * @return 200 OK with calculated pricing details, or 400 Bad Request if inputs are invalid
     */
    @PostMapping("/calculate")
    public ResponseEntity<PricingResponse> calculatePrice(@RequestBody PricingRequest request) {
        int tenureMonths = request.effectiveTenureMonths();
        BigDecimal monthlyPrice = pricingService.calculateMonthlyPrice(
            request.tier(),
            tenureMonths,
            request.voucher()
        );

        return ResponseEntity.ok(new PricingResponse(
            request.tier(),
            tenureMonths,
            request.voucher(),
            monthlyPrice
        ));
    }
}
