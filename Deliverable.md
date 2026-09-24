# Order Service Investigation & Root Cause Analysis

---

# Root Cause

## 1. The Error

When running Scenario B or executing the unit test suite (`mvn test`), the application crashes with an unhandled **`java.lang.NullPointerException`**:

```text
java.lang.NullPointerException: Cannot invoke "com.rsl.orderservice.model.Coupon.getPercentOff()" because "coupon" is null
```

* **Runtime failure**: `Scenario B: order with unknown coupon BLACKFRIDAY` fails to complete order placement.
* **Test failure**: `DiscountServiceTest.unknownCouponCodeIsIgnoredNotFatal` fails because the test explicitly expects unknown coupons to be ignored without throwing an exception.

---

## 2. Root Cause

In `DiscountService.discountCents()`, the code checks that `couponCode` is non-null and not blank, but makes an invalid assumption that any provided coupon code must exist in `CouponRepository`.

When a customer enters a coupon code that isn't registered in the repository (such as `"BLACKFRIDAY"`):
1. `couponRepository.findByCode(couponCode)` returns `null`.
2. The code immediately dereferences `coupon.getPercentOff()` on lines 52 and 53 without performing a null check.
3. Because `coupon` is `null`, Java throws a `NullPointerException`.

As stated by the test specification in `DiscountServiceTest.java`:
> *"A coupon code the customer mistyped must not bring the order down."*

The business intent is for non-existent coupons to simply contribute 0% discount rather than aborting the transaction.

---

## 3. Evidence

### A. Log Line
From `logs/app.log` (lines 20–22):
```text
2026-09-24 13:07:34 [INFO] InventoryService - Reserved 1 of MUG-010 (was 3, now 2)
2026-09-24 13:07:34 [SEVERE] App - Scenario failed: B: order with unknown coupon BLACKFRIDAY
java.lang.NullPointerException: Cannot invoke "com.rsl.orderservice.model.Coupon.getPercentOff()" because "coupon" is null
```

### B. Stack-Trace Line
The top frame of the stack trace pointing directly to the offending line (line 23 of `logs/app.log`):
```text
at com.rsl.orderservice.service.DiscountService.discountCents(DiscountService.java:52)
at com.rsl.orderservice.service.OrderService.placeOrder(OrderService.java:78)
at com.rsl.orderservice.App.lambda$main$1(App.java:63)
```

### C. Source Code Line
In `src/main/java/com/rsl/orderservice/service/DiscountService.java` (lines 50–54):
```java
if (couponCode != null && !couponCode.isBlank()) {
    Coupon coupon = couponRepository.findByCode(couponCode);
    log.info("Applying coupon '" + couponCode + "' -> " + coupon.getPercentOff() + "%"); // Line 52: NPE here
    percent += coupon.getPercentOff();                                                   // Line 53
}
```

In `src/main/java/com/rsl/orderservice/repository/CouponRepository.java` (lines 18–20):
```java
public Coupon findByCode(String code) {
    return coupons.get(code); // Returns null when key 'BLACKFRIDAY' is not present
}
```

---

## 4. Secondary Impact: Leaked Inventory Reservation

Beyond the crash itself, an order execution ordering issue exists in `OrderService.placeOrder()`:
1. `inventoryService.reserve(sku, qty)` is called on line 71, decrementing the available stock.
2. `discountService.discountCents(...)` is called on line 78 and throws the NPE.
3. The method aborts without rolling back or releasing the reserved stock.

As shown in `logs/app.log`:
```text
2026-09-24 13:07:34 [INFO] InventoryService - Reserved 1 of MUG-010 (was 3, now 2)
```
The stock for `MUG-010` was decremented from 3 to 2, but the order was never placed or cancelled.

---

### Task 1 prompt
```text
Read the README.md file in #2 of rsllearning/rsl-training-pankaj-kumar repo and follow its instructions to build and run the application. Confirm that one scenario fails at runtime and one test fails. Find the error written to logs/app.log and summarize the observed failures.
```

### Output
* **Test Suite Failure (`mvn test`)**: 1 test failed out of 12.
  - **Failing Test**: [`DiscountServiceTest.unknownCouponCodeIsIgnoredNotFatal`](file:///Users/pankaj.kumar/Desktop/order-service-app/rsl-training-pankaj-kumar/src/test/java/com/rsl/orderservice/DiscountServiceTest.java#L30-L36)
  - **Failure Detail**: Threw unexpected `java.lang.NullPointerException` when evaluating an unregistered coupon (`assertDoesNotThrow` failed).
* **Runtime Application Failure (`mvn -q compile exec:java`)**:
  - **Scenario A (`SAVE10`)**: Completed successfully with 10% coupon applied.
  - **Scenario B (`BLACKFRIDAY`)**: Failed at runtime with `java.lang.NullPointerException: Cannot invoke "com.rsl.orderservice.model.Coupon.getPercentOff()" because "coupon" is null`.
* **Logged Error in [`logs/app.log`](file:///Users/pankaj.kumar/Desktop/order-service-app/rsl-training-pankaj-kumar/logs/app.log#L21-L31)**:
  - Recorded a `SEVERE` entry for Scenario B with a stack trace originating at [`DiscountService.discountCents(DiscountService.java:52)`](file:///Users/pankaj.kumar/Desktop/order-service-app/rsl-training-pankaj-kumar/src/main/java/com/rsl/orderservice/service/DiscountService.java#L52).

---

### Task 2 prompt
```text
In #2 of rsllearning/rsl-training-pankaj-kumar repo, identify the appropriate MCP server that can access the locally generated logs/app.log file, since GitHub MCP cannot access runtime-generated local files. Set up the appropriate MCP server and use it to retrieve and inspect the contents of logs/app.log.
```

### Output
* **Server Identification**:
  - **GitHub MCP Limitation**: Interacts exclusively with remote GitHub API endpoints (commits, PRs, git tree objects) and cannot read untracked, runtime-generated local disk files.
  - **Appropriate MCP Server**: The official **Filesystem MCP Server** (`@modelcontextprotocol/server-filesystem`).
* **MCP Server Setup**:
  - Configured in [`~/.gemini/config/mcp_config.json`](file:///Users/pankaj.kumar/.gemini/config/mcp_config.json) using `npx -y @modelcontextprotocol/server-filesystem` scoped to the workspace directory (`/Users/pankaj.kumar/Desktop/order-service-app/rsl-training-pankaj-kumar`).
* **Log Retrieval & Inspection**:
  - Successfully retrieved and inspected [`logs/app.log`](file:///Users/pankaj.kumar/Desktop/order-service-app/rsl-training-pankaj-kumar/logs/app.log) via filesystem MCP tools (`read_file` / `read_text_file`), confirming the exact timestamped log entries and stack trace for Scenario B

---

### Task 3 prompt
```text
Read the error in logs/app.log from rsllearning/rsl-training-pankaj-kumar and create a GitHub issue in the repository describing the observed error. Include a clear title, observed behaviour, and steps to reproduce. Describe only the error and observed behaviour; do not speculate about the root cause or propose a fix. The issue will be referenced in the pull request.
```

### Output
* **GitHub Issue Created**: [#3](https://github.com/rsllearning/rsl-training-pankaj-kumar/issues/3)
* **Title**: `NullPointerException in DiscountService.discountCents when placing order with unknown coupon`
* **Issue Structure**:
  - **Observed Behaviour**: Documented the runtime crash when placing an order with an unknown/unregistered coupon code (e.g., `"BLACKFRIDAY"`), including the exact `java.lang.NullPointerException` stack trace and relevant entries from `logs/app.log`.
  - **Steps to Reproduce**: Provided concise step-by-step reproduction instructions executing `App.main()` with an unrecognized coupon code without speculating on root cause or proposing fixes.

---

### Task 4 prompt
```text
Read the runtime error and stack trace in rsllearning/rsl-training-pankaj-kumar and walk through the stack trace. Identify the exact file and line where the error was thrown, and describe the call path that led to that line.
```

### Output
* **Runtime Error & Stack Trace**:
  ```text
  2026-09-24 13:07:34 [SEVERE] App - Scenario failed: B: order with unknown coupon BLACKFRIDAY
  java.lang.NullPointerException: Cannot invoke "com.rsl.orderservice.model.Coupon.getPercentOff()" because "coupon" is null
      at com.rsl.orderservice.service.DiscountService.discountCents(DiscountService.java:52)
      at com.rsl.orderservice.service.OrderService.placeOrder(OrderService.java:78)
      at com.rsl.orderservice.App.lambda$main$1(App.java:63)
      at com.rsl.orderservice.App.runScenario(App.java:79)
      at com.rsl.orderservice.App.main(App.java:62)
      at org.codehaus.mojo.exec.ExecJavaMojo.doMain(ExecJavaMojo.java:385)
      at org.codehaus.mojo.exec.ExecJavaMojo.doExec(ExecJavaMojo.java:374)
      at org.codehaus.mojo.exec.ExecJavaMojo.lambda$execute$0(ExecJavaMojo.java:296)
      at java.base/java.lang.Thread.run(Thread.java:1583)
  ```
* **Exact File & Line Thrown**:
  - **File**: `com/rsl/orderservice/service/DiscountService.java`
  - **Line**: **Line 52**
  - **Offending Code**: `log.info("Applying coupon '" + couponCode + "' -> " + coupon.getPercentOff() + "%");`
  - **Cause**: When an unregistered coupon (`BLACKFRIDAY`) is passed, `couponRepository.findByCode(couponCode)` returns `null`. Dereferencing `coupon.getPercentOff()` on line 52 throws `java.lang.NullPointerException`.
* **Call Path Walkthrough**:
  1. `App.main(App.java:62)`: Initiates Scenario B by passing a lambda to `runScenario()`.
  2. `App.runScenario(App.java:79)`: Executes `scenario.run()` inside a `try-catch` block.
  3. `App.lambda$main$1(App.java:63)`: Lambda calls `orderService.placeOrder("ORD-B", bob, ..., "BLACKFRIDAY")`.
  4. `OrderService.placeOrder(OrderService.java:78)`: Calls `discountService.discountCents(subtotal, customer, couponCode)`.
  5. `DiscountService.discountCents(DiscountService.java:52)`: Dereferences `coupon.getPercentOff()` while `coupon` is `null`, throwing the `NullPointerException`.

---

### Task 5 prompt
```text
Analyse the error and stack trace in rsllearning/rsl-training-pankaj-kumar and identify the root cause. Clearly separate the observed issue from the root cause, and cite the specific code behaviour or implementation that supports the conclusion. Do not propose a fix yet.
```

### Output
* **Observed Issue**:
  - `java.lang.NullPointerException: Cannot invoke "com.rsl.orderservice.model.Coupon.getPercentOff()" because "coupon" is null` thrown during runtime (`Scenario B`) and unit tests (`DiscountServiceTest.unknownCouponCodeIsIgnoredNotFatal`).
  - Halts order execution and leaves reserved inventory (`MUG-010`) in an unreleased/orphaned state.
* **Root Cause**:
  - **Unchecked Assumption of Entity Existence**: `DiscountService.discountCents` assumes any non-blank `couponCode` string exists in the repository.
  - When an unregistered code is provided, `CouponRepository.findByCode()` returns `null`, which `DiscountService` dereferences without validation, directly violating the business rule that unknown coupons should be safely ignored.
* **Supporting Code Implementation & Behaviour**:
  - [`CouponRepository.java:L18-L20`](src/main/java/com/rsl/orderservice/repository/CouponRepository.java#L18-L20): Contract explicitly returns `coupons.get(code)`, yielding `null` when a key does not exist.
  - [`DiscountService.java:L50-L54`](src/main/java/com/rsl/orderservice/service/DiscountService.java#L50-L54): Only validates `couponCode != null && !couponCode.isBlank()`, then dereferences `coupon.getPercentOff()` on lines 52 and 53 without checking if `coupon != null`.
  - [`DiscountServiceTest.java:L34-L35`](src/test/java/com/rsl/orderservice/DiscountServiceTest.java#L34-L35): Test specification establishes the requirement: *"A coupon code the customer mistyped must not bring the order down."*

---

### Task 6 prompt
```text
Based on the identified root cause in rsllearning/rsl-training-pankaj-kumar, propose one or more possible fixes. For each option, explain why it addresses the root cause, and identify which fix you recommend based on the available code and evidence.
```

### Output
* **Option 1: Null Check with Fallback Logging in `DiscountService` (Recommended)**:
  - **Proposed Change**: Check `if (coupon != null)` in `DiscountService.discountCents()` after looking up the code. If non-null, apply discount; otherwise, log a warning and proceed without applying a coupon discount.
  - **Why it addresses the root cause**: Directly guards against `null` returns from `CouponRepository.findByCode()`, preventing `NullPointerException` on dereference and adhering to the specification that unknown coupons should be safely ignored.
* **Option 2: Refactor `CouponRepository.findByCode()` to Return `Optional<Coupon>`**:
  - **Proposed Change**: Change repository return type to `Optional<Coupon>` and update callers to handle absence explicitly.
  - **Why it addresses the root cause**: Enforces null-safety at compile time across callers.
  - **Drawback**: Expands API changes across the repository and existing test callers unnecessarily.
* **Option 3: Null Object Pattern in `CouponRepository`**:
  - **Proposed Change**: Return a dummy `new Coupon(code, 0)` when code is not found.
  - **Why it addresses the root cause**: Guarantees non-null return values so callers never hit NPEs.
  - **Drawback**: Masking non-existent coupons as real coupon objects obscures domain state and invalidates distinguishing valid 0% promotions from mistyped codes.
* **Recommendation**:
  - **Option 1** is recommended. It has the minimal blast radius, keeps repository contracts consistent with other repositories (e.g. `CustomerRepository`), satisfies [`DiscountServiceTest.unknownCouponCodeIsIgnoredNotFatal`](src/test/java/com/rsl/orderservice/DiscountServiceTest.java#L30-L36), and preserves clear logging semantics.

---

### Task 7 prompt
```text
Before modifying rsllearning/rsl-training-pankaj-kumar, explain the proposed change and confirm how it addresses the identified root cause. Then apply the agreed fix locally using the MCP server configured for local file access. After applying the change, rebuild and re-run the application and unit tests. Confirm that the runtime crash is resolved and that no previously passing tests have regressed.
```

### Output
* **Fix Explanation & Root Cause Addressal**:
  - In `DiscountService.discountCents()`, guarded against `null` returns from `couponRepository.findByCode(couponCode)` by validating `coupon != null` before invoking `coupon.getPercentOff()`.
  - If null, logs a warning (`"Coupon code '...' is invalid or expired; ignoring"`) and contributes 0% discount without aborting execution, directly addressing the root cause and fulfilling test specifications.
* **Local Modification via MCP**:
  - Applied the change directly to [`src/main/java/com/rsl/orderservice/service/DiscountService.java`](src/main/java/com/rsl/orderservice/service/DiscountService.java) using the **Filesystem MCP Server** (`edit_file` tool).
* **Build & Unit Test Verification (`mvn test`)**:
  - All 12 unit tests passed (`Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`).
  - [`DiscountServiceTest.unknownCouponCodeIsIgnoredNotFatal`](src/test/java/com/rsl/orderservice/DiscountServiceTest.java#L30-L36) passed successfully.
* **Runtime Application Execution (`mvn -q compile exec:java`)**:
  - **Scenario A (`SAVE10`)**: Completed successfully with 10% coupon applied.
  - **Scenario B (`BLACKFRIDAY`)**: Completed successfully; unknown coupon was gracefully logged as invalid/expired with 0% discount and order `ORD-B` was confirmed.
  - **Result**: Runtime crash resolved and zero regressions across existing tests.

---

### Task 8 prompt
```text
In rsllearning/rsl-training-pankaj-kumar, create a branch named fix/unknown-coupon_issue for the fix. Commit the changes with a clear commit message that references the related GitHub issue, then create a pull request with a clear description covering the issue, observed behaviour, root cause, implemented fix, and verification results. Perform the branch creation, commit, and pull request operations remotely through the GitHub MCP server.
```

### Output
* **Branch Created**: `fix/unknown-coupon_issue` (branched from `pankaj_kumar_debugging_using_mcp`)
* **Commit Message**: `fix: handle unknown coupon codes gracefully in DiscountService (#3)`
* **Pull Request**: [#4](https://github.com/rsllearning/rsl-training-pankaj-kumar/pull/4) — `fix: handle unknown coupon codes gracefully in DiscountService (#3)`
  - **Base Branch**: `pankaj_kumar_debugging_using_mcp`
  - **Head Branch**: `fix/unknown-coupon_issue`
  - **PR Structure & Contents**:
    - **Related Issue**: Linked and closed [#3](https://github.com/rsllearning/rsl-training-pankaj-kumar/issues/3).
    - **Observed Behaviour**: Documented the unhandled `NullPointerException` thrown in `DiscountService.discountCents` during Scenario B and unit test failure.
    - **Root Cause**: Unchecked assumption of coupon existence in `CouponRepository`.
    - **Implemented Fix**: Null-check guarding in `DiscountService.discountCents()` with fallback warning logging and 0% discount.
    - **Verification Results**: 12/12 unit tests passing (`mvn clean test`) and successful end-to-end execution of Scenarios A and B (`mvn -q compile exec:java`).
