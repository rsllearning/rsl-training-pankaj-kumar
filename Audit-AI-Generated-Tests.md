# Audit & Refactoring: AI-Generated Tests

Review and cleanup of the AI-generated unit tests for `SubscriptionPricingServiceTest`.

---

## Key Issues Found & Fixes Applied

### 1. Test Structure & Organization
* **Numbered Display Names**: The AI prefixed `@DisplayName` with numbers (`"1. Base Rates"`, `"2. Longevity..."`). This falsely implied test execution order, cluttered test runner outputs, and created renumbering overhead.
  * **Fix**: Removed all numeric prefixes in favor of clean, behavior-driven titles.
* **Artificial "Junk-Drawer" Validation Class**: All validations and edge cases were dumped into a separate `EdgeCasesAndValidationTests` class, breaking cohesion with the features they belong to.
  * **Fix**: Disbanded the separate class and co-located edge cases directly within their domain classes (`BaseRatesTests` for null tier, `LongevityDiscountBoundariesTests` for negative/extreme tenure, and `PromotionalVouchersTests` for blank/invalid vouchers).

### 2. Contract & API Alignment
* **Wrong Method Name & Invented Overload**: Tests targeted `calculatePrice` instead of `calculateMonthlyPrice`, and invented an unsupported 2-argument overload (`tier, tenure`).
  * **Fix**: Standardized all calls to `calculateMonthlyPrice(tier, tenure, voucher)`.
* **Weak / Tautological Assertions**: Exception checks used `contains(voucher) || !isBlank()`, which passed on *any* non-blank string, completely bypassing voucher verification.
  * **Fix**: Changed to strict assertions verifying that the exception message explicitly references the offending voucher or invalid argument.

### 3. Boundary Coverage & Duplication
* **Missing Boundary Combinations**: Voucher tests only covered months 0, 13, and 37, omitting the critical upper-bound transition months at 12 and 36.
  * **Fix**: Added explicit test cases for 12m and 36m with both `SAVE20` and `HALFPRICE`.
* **Redundant Tests & Misleading Floor Checks**: A separate financial section duplicated calculations already tested earlier, and included a weak `>= 0` assertion on a $17.50 result claiming to test the $0.00 floor.
  * **Fix**: Removed duplicate calculation blocks; currency scale (`scale() == 2`) and exact amounts are enforced directly in all concrete domain assertions.

### 4. Realistic Scope vs. Hallucinated Tests
* **Irrelevant Web Attack Vectors**: Tests used SQL injection (`' OR '1'='1`), XSS (`<script>`), and path traversal (`../../etc/passwd`) on an in-memory pricing service.
  * **Fix**: Replaced web exploit payloads with realistic voucher edge cases (case-sensitivity, whitespace/tabs, hyphenated codes, and unknown promo tokens).
* **Over-Constrained Precedence**: Tests attempted to enforce that argument validation runs before voucher validation when both are invalid.
  * **Fix**: Removed artificial validation ordering constraints and redundant multi-failure tests.

---

## Final Test Suite Layout

The refactored suite is organized into **3 cohesive domain classes**:

1. **`BaseRatesTests`** – Base tier pricing ($50, $150, $500) and `null` tier rejection.
2. **`LongevityDiscountBoundariesTests`** – Explicit boundaries (12m, 13m, 36m, 37m), interior partitions, negative tenure rejection, and `Integer.MAX_VALUE` overflow protection.
3. **`PromotionalVouchersTests`** – `SAVE20` ($20 deduction) and `HALFPRICE` (50% deduction) across base and longevity boundaries, graceful blank voucher handling, and `InvalidVoucherException` enforcement.