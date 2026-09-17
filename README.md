# Subscription Pricing Service

A Spring Boot 3 microservice built using strict Test-Driven Development (TDD) and Java 21 to calculate monthly subscription prices based on tier, tenure, and promotional vouchers.

---

## Business Rules

- **Base Tiers**: `BASIC` ($50.00/mo), `PRO` ($150.00/mo), `ENTERPRISE` ($500.00/mo)
- **Tenure Discounts**:
  - `0 - 12 months`: 0% discount
  - `13 - 36 months`: 10% discount
  - `37+ months`: 25% discount
- **Promotional Vouchers**:
  - `SAVE20`: Flat $20.00 deduction (applied after tenure discount)
  - `HALFPRICE`: 50% discount on tenure-adjusted price
  - Blank/null vouchers apply no discount; invalid codes return HTTP 400 Bad Request
- **Financial Rules**: Calculated using `BigDecimal`, rounded to 2 decimal places (`HALF_UP`), and clamped to a `$0.00` minimum price floor.

---

## REST API

### `POST /api/subscriptions/calculate`

**Request:**
```json
{
  "tier": "PRO",
  "tenureMonths": 14,
  "voucher": "SAVE20"
}
```

**Response (`200 OK`):**
```json
{
  "tier": "PRO",
  "tenureMonths": 14,
  "voucher": "SAVE20",
  "monthlyPrice": 115.00
}
```

---

## Quickstart

```bash
# Run all unit and integration tests (130 tests)
./mvnw clean test

# Run application locally (port 8080)
./mvnw spring-boot:run
```

---

## Documentation & Assignment Artifacts

- **[Audit-AI-Generated-Tests.md](Audit-AI-Generated-Tests.md)**: Deep-dive analysis and refactoring log of initial AI-generated tests.
- **[Task-5.md](Task-5.md)**: Adversarial edge cases, engineering justifications, and test hardening reports.
- **[REFLECTION.md](REFLECTION.md)**: Engineering reflections on AI productivity, failure modes, and TDD guardrails.