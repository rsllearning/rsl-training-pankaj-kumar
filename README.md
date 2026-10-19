# Online Shopping Database - Advanced SQL Assignment

## 1. Assignment Overview
This project contains the complete solution for the database modernization initiative of an online shopping platform. It demonstrates advanced SQL concepts including complex joins, subqueries, set operations, window functions, CTEs (including recursive CTEs), views, indexes, stored procedures, user-defined functions, transactions, triggers, and query performance tuning.

---

## 2. Database Schema

The database consists of the following relational tables:

- **Customers**
  - `customer_id` (INT, PK, AUTO_INCREMENT)
  - `customer_name` (VARCHAR(100))
  - `city` (VARCHAR(50))
  - `email` (VARCHAR(100), UNIQUE)

- **Products**
  - `product_id` (INT, PK, AUTO_INCREMENT)
  - `product_name` (VARCHAR(100))
  - `category` (VARCHAR(50))
  - `price` (DECIMAL(10,2))
  - `created_date` (DATE)

- **Orders**
  - `order_id` (INT, PK, AUTO_INCREMENT)
  - `customer_id` (INT, FK -> Customers.customer_id)
  - `product_id` (INT, FK -> Products.product_id)
  - `quantity` (INT)
  - `order_date` (DATE)

- **Order_Audit** *(Created for Task 19)*
  - `audit_id` (INT, PK, AUTO_INCREMENT)
  - `order_id` (INT)
  - `customer_id` (INT)
  - `product_id` (INT)
  - `quantity` (INT)
  - `order_date` (DATE)
  - `audit_action` (VARCHAR(50))
  - `created_at` (TIMESTAMP)

---

## 3. Steps to Execute the SQL Script

You can execute the SQL script using MySQL Workbench, DBeaver, or via the command line interface (CLI):

### Option A: Using MySQL Command Line Client
```bash
mysql -u <username> -p < advanced_sql_assignment.sql
```

### Option B: Using MySQL Shell or GUI Client
1. Open MySQL Workbench / DBeaver / phpMyAdmin.
2. Open [`advanced_sql_assignment.sql`](advanced_sql_assignment.sql).
3. Execute the entire script or run sections sequentially.

---

## 4. Summary of Implemented Advanced SQL Concepts

- **Section 1: Advanced Joins**
  - `INNER JOIN` across three tables (`Customers`, `Orders`, `Products`).
  - `CROSS JOIN` to generate Cartesian product of all customers and products.
  - `LEFT JOIN` to identify all customers, including those with no order history.
- **Section 2: Subqueries**
  - Scalar subquery comparing against `AVG(price)`.
  - Correlated subquery in `HAVING` clause to filter customers exceeding overall average spending.
  - Correlated subquery to find highest-priced product per category.
- **Section 3: Set Operations**
  - `UNION` combining product subsets from distinct categories.
  - MySQL alternatives for `INTERSECT` (via `INNER JOIN`) and `EXCEPT` (via `LEFT JOIN ... WHERE IS NULL`).
- **Section 4: Window Functions**
  - `RANK()` and `DENSE_RANK()` partitioned by category and ordered by price.
  - `LAG()` to fetch previous product price and running total via `SUM() OVER (...)`.
- **Section 5: Common Table Expressions (CTEs)**
  - Multi-level CTE (`CustomerPurchases` and `AveragePurchase`) to isolate above-average spenders.
  - `WITH RECURSIVE` CTE generating sequential integers 1 through 10.
- **Section 6: Views**
  - Created view `View_HighPricedProducts` encapsulating products priced > ₹1,000.
- **Section 7: Indexes & Query Performance**
  - Created B-tree index on `Products(category)`.
  - Analyzed query execution plan using `EXPLAIN`.
- **Section 8: Stored Procedures & Functions**
  - Stored Procedure `GetProductsByCategory` accepting an input parameter.
  - Deterministic Function `CalculateOrderTotal` returning computed order values (`price × quantity`).
- **Section 9: Transactions**
  - ACID transaction demonstration showcasing `START TRANSACTION`, `UPDATE`, `COMMIT`, and `ROLLBACK`.
- **Section 10: SQL Triggers**
  - Created `AFTER INSERT` trigger `trg_after_order_insert` on `Orders` table logging entries into `Order_Audit`.
- **Section 11: Performance Tuning**
  - Optimized anti-pattern query:
    - Avoided wildcard `SELECT *` by selecting only required columns.
    - Removed redundant `DISTINCT` overhead.
    - Restored SARGability by replacing `YEAR(created_date) = 2024` with range comparison `>= '2024-01-01' AND < '2025-01-01'`.
    - Created composite index on `(category, created_date)`.