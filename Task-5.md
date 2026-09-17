# Task 5 - Edge Cases & Test Hardening

### AI Brainstorm List
- $0 floor clamp is dead code right now since lowest price is $17.50 ($50 * 0.75 - 20).
- Controller was missing 12m and 36m boundary transition tests.
- Weird vouchers like Cyrillic homoglyph `SАVE20`, zero-width space `\u200B`, BOM prefix, lowercase `save20`.
- Omitted or null tenure in JSON request body causing possible NPE.
- Scale and precision issues on fractional prices ($18.75, $56.25).

---

### 4 Edge Case Justifications
1. **Weird / Homoglyph Vouchers (`SАVE20`, `\u200B`)**: Cyrillic letters or hidden spaces can trick string checks or throw 500 error. Needed to ensure API always returns clean 400 Bad Request.
2. **Longevity Boundries (12/13m & 36/37m)**: Off-by-one errors here cause $50-$75 price jumps on Enterprise. Controller was missing 12m and 36m tests completely.
3. **Null / Omitted Tenure**: Frontend often skips optional fields or passes null. If unboxed directly it throws NPE (500), so we verified it safely defaults to 0.
4. **Fractional Cent Precision**: Half price on discounted tiers gives fractions like $18.75. Tests make sure scale stays strictly at 2 decimals for payment gateways.

---

### Updated Test Files
- `SubscriptionPricingServiceTest.java`: moved to `service` package, added unicode voucher tests, removed duplicates.
- `SubscriptionPricingControllerTest.java`: added parameterized boundary (12/13m, 36/37m) and omitted/null tenure tests.
- Removed unused `.gitkeep` and empty `test/resources` folder.

---

### Test Execution Log
```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.rsl.training.subscription.controller.SubscriptionPricingControllerTest
[INFO] Tests run: 40, Failures: 0, Errors: 0, Skipped: 0 -- in com.rsl.training.subscription.controller.SubscriptionPricingControllerTest
[INFO] Running com.rsl.training.subscription.service.SubscriptionPricingServiceTest
[INFO] Running com.rsl.training.subscription.service.SubscriptionPricingServiceTest$PromotionalVouchersTests
[INFO] Tests run: 58, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.rsl.training.subscription.service.SubscriptionPricingServiceTest$LongevityDiscountBoundariesTests
[INFO] Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.rsl.training.subscription.service.SubscriptionPricingServiceTest$BaseRatesTests
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 130, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```