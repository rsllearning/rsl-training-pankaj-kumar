-- Database Initialization & Schema Setup

CREATE DATABASE online_shopping;

USE online_shopping;


CREATE TABLE Customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_date DATE DEFAULT (CURRENT_DATE)
);

CREATE TABLE Orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    order_date DATE NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);


-- Sample Data Insertion
-- 10 Customers
INSERT INTO Customers (customer_id, customer_name, city, email) VALUES
(1, 'Aarav Sharma', 'Mumbai', 'aarav.sharma@example.com'),
(2, 'Diya Patel', 'Ahmedabad', 'diya.patel@example.com'),
(3, 'Rohan Verma', 'Delhi', 'rohan.verma@example.com'),
(4, 'Ananya Iyer', 'Bangalore', 'ananya.iyer@example.com'),
(5, 'Vikram Singh', 'Jaipur', 'vikram.singh@example.com'),
(6, 'Neha Gupta', 'Pune', 'neha.gupta@example.com'),
(7, 'Aditya Rao', 'Hyderabad', 'aditya.rao@example.com'),
(8, 'Pooja Nair', 'Kochi', 'pooja.nair@example.com'),
(9, 'Karan Malhotra', 'Chandigarh', 'karan.malhotra@example.com'),
(10, 'Sneha Mukherjee', 'Kolkata', 'sneha.mukherjee@example.com');

-- 10 Products
INSERT INTO Products (product_id, product_name, category, price, created_date) VALUES
(1, 'Laptop', 'Electronics', 65000.00, '2024-01-15'),
(2, 'Smartphone', 'Electronics', 35000.00, '2024-02-10'),
(3, 'Wireless Headphones', 'Electronics', 3000.00, '2024-03-05'),
(4, 'Smartwatch', 'Electronics', 4500.00, '2024-04-12'),
(5, 'Coffee Maker', 'Home Appliances', 2500.00, '2024-02-20'),
(6, 'Microwave Oven', 'Home Appliances', 8500.00, '2024-05-18'),
(7, 'Electric Kettle', 'Home Appliances', 950.00, '2024-06-01'),
(8, 'Running Shoes', 'Footwear', 2200.00, '2024-03-25'),
(9, 'Leather Boots', 'Footwear', 4200.00, '2024-04-02'),
(10, 'Sandals', 'Footwear', 800.00, '2024-05-10');

-- 22 Orders
INSERT INTO Orders (order_id, customer_id, product_id, quantity, order_date) VALUES
(1, 1, 1, 1, '2024-06-01'),
(2, 1, 3, 2, '2024-06-02'),
(3, 2, 2, 1, '2024-06-03'),
(4, 2, 5, 1, '2024-06-04'),
(5, 3, 4, 1, '2024-06-05'),
(6, 3, 6, 1, '2024-06-06'),
(7, 4, 1, 1, '2024-06-07'),
(8, 4, 7, 2, '2024-06-08'),
(9, 5, 8, 2, '2024-06-09'),
(10, 5, 9, 1, '2024-06-10'),
(11, 6, 2, 1, '2024-06-11'),
(12, 6, 3, 1, '2024-06-12'),
(13, 7, 5, 2, '2024-06-13'),
(14, 7, 10, 3, '2024-06-14'),
(15, 8, 6, 1, '2024-06-15'),
(16, 8, 8, 1, '2024-06-16'),
(17, 9, 3, 3, '2024-06-17'),
(18, 9, 4, 1, '2024-06-18'),
(19, 1, 8, 1, '2024-06-19'),
(20, 2, 9, 2, '2024-06-20'),
(21, 3, 2, 1, '2024-06-21'),
(22, 4, 5, 1, '2024-06-22');


-- Section 1: Advanced Joins

-- Task 1: Display each customer along with the products they ordered.
SELECT 
    c.customer_id,
    c.customer_name,
    p.product_id,
    p.product_name,
    o.order_id,
    o.quantity,
    o.order_date
FROM Customers c
JOIN Orders o ON c.customer_id = o.customer_id
JOIN Products p ON o.product_id = p.product_id;

-- Task 2: Display all possible combinations of customers and products using a CROSS JOIN.
SELECT 
    c.customer_id,
    c.customer_name,
    p.product_id,
    p.product_name
FROM Customers c
CROSS JOIN Products p;

-- Task 3: Display all customers and their orders, including customers who have not placed any orders.
SELECT 
    c.customer_id,
    c.customer_name,
    c.city,
    o.order_id,
    o.product_id,
    o.quantity,
    o.order_date
FROM Customers c
LEFT JOIN Orders o ON c.customer_id = o.customer_id;


-- Section 2: Subqueries

-- Task 4: Find products priced above the average product price.
SELECT 
    product_id,
    product_name,
    category,
    price
FROM Products
WHERE price > (SELECT AVG(price) FROM Products);

-- Task 5: Display customers whose total purchase amount is greater than the average customer purchase amount.
SELECT 
    c.customer_id,
    c.customer_name,
    SUM(p.price * o.quantity) AS total_spent
FROM Customers c
JOIN Orders o ON c.customer_id = o.customer_id
JOIN Products p ON o.product_id = p.product_id
GROUP BY c.customer_id, c.customer_name
HAVING total_spent > (
    SELECT AVG(customer_spending)
    FROM (
        SELECT SUM(p2.price * o2.quantity) AS customer_spending
        FROM Orders o2
        JOIN Products p2 ON o2.product_id = p2.product_id
        GROUP BY o2.customer_id
    ) AS avg_cust_spending
);

-- Task 6: Find the highest-priced product in each category using a correlated subquery.
SELECT 
    p1.product_id,
    p1.product_name,
    p1.category,
    p1.price
FROM Products p1
WHERE p1.price = (
    SELECT MAX(p2.price)
    FROM Products p2
    WHERE p2.category = p1.category
);


-- Section 3: Set Operations

-- Task 7: Combine products from two different categories using UNION.
SELECT product_id, product_name, category, price
FROM Products
WHERE category = 'Electronics'
UNION
SELECT product_id, product_name, category, price
FROM Products
WHERE category = 'Home Appliances';

-- Task 8: Write MySQL alternatives for INTERSECT and EXCEPT using INNER JOIN and LEFT JOIN.
SELECT DISTINCT c.customer_id, c.customer_name
FROM Customers c
INNER JOIN Orders o ON c.customer_id = o.customer_id
WHERE c.city = 'Mumbai';

-- Alternative for EXCEPT (A EXCEPT B): Customers who have NOT placed any orders
SELECT c.customer_id, c.customer_name
FROM Customers c
LEFT JOIN Orders o ON c.customer_id = o.customer_id
WHERE o.order_id IS NULL;


-- Section 4: Window Functions

-- Task 9: Rank products by price within each category.
SELECT 
    product_id,
    product_name,
    category,
    price,
    RANK() OVER (PARTITION BY category ORDER BY price DESC) AS price_rank,
    DENSE_RANK() OVER (PARTITION BY category ORDER BY price DESC) AS dense_price_rank
FROM Products;

-- Task 10: Display the previous product price (LAG) and a running total of product prices using window functions.
SELECT 
    product_id,
    product_name,
    category,
    price,
    LAG(price, 1) OVER (ORDER BY price) AS previous_price,
    SUM(price) OVER (ORDER BY price ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_total_price
FROM Products;


-- Section 5: Common Table Expressions (CTEs)

-- Task 11: Create a CTE to display customers whose total purchase amount is above the average.
WITH CustomerPurchases AS (
    SELECT 
        c.customer_id,
        c.customer_name,
        SUM(p.price * o.quantity) AS total_purchase
    FROM Customers c
    JOIN Orders o ON c.customer_id = o.customer_id
    JOIN Products p ON o.product_id = p.product_id
    GROUP BY c.customer_id, c.customer_name
),
AveragePurchase AS (
    SELECT AVG(total_purchase) AS avg_purchase FROM CustomerPurchases
)
SELECT 
    cp.customer_id,
    cp.customer_name,
    cp.total_purchase,
    ROUND(ap.avg_purchase, 2) AS overall_avg_purchase
FROM CustomerPurchases cp
CROSS JOIN AveragePurchase ap
WHERE cp.total_purchase > ap.avg_purchase;

-- Task 12: Create a recursive CTE to generate a sequence of numbers from 1 to 10.
WITH RECURSIVE NumberSequence AS (
    SELECT 1 AS num
    UNION ALL
    SELECT num + 1 FROM NumberSequence WHERE num < 10
)
SELECT num FROM NumberSequence;


-- Section 6: Views

-- Task 13: Create a view that displays products priced above ₹1,000 and retrieve data from the view.
CREATE OR REPLACE VIEW View_HighPricedProducts AS
SELECT 
    product_id,
    product_name,
    category,
    price
FROM Products
WHERE price > 1000.00;

-- Retrieve data from the view
SELECT * FROM View_HighPricedProducts;


-- Section 7: Indexes and Query Performance

-- Task 14: Create an index on the category column of the Products table.
CREATE INDEX idx_products_category ON Products(category);

-- Task 15: Use EXPLAIN to analyze a query that filters products by category.
EXPLAIN SELECT product_id, product_name, category, price
FROM Products
WHERE category = 'Electronics';



-- Section 8: Stored Procedures and Functions

-- Task 16: Create a stored procedure to retrieve products by category.
DROP PROCEDURE IF EXISTS GetProductsByCategory;

DELIMITER $$
CREATE PROCEDURE GetProductsByCategory(IN input_category VARCHAR(50))
BEGIN
    SELECT 
        product_id,
        product_name,
        category,
        price
    FROM Products
    WHERE category = input_category;
END$$
DELIMITER ;

-- Procedure Execution Example:
CALL GetProductsByCategory('Electronics');

-- Task 17: Create a function to calculate the total price of an order (price × quantity).
DROP FUNCTION IF EXISTS CalculateOrderTotal;

DELIMITER $$
CREATE FUNCTION CalculateOrderTotal(unit_price DECIMAL(10,2), qty INT)
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    RETURN unit_price * qty;
END$$
DELIMITER ;

SELECT 
    order_id,
    product_id,
    quantity,
    CalculateOrderTotal(500.00, quantity) AS sample_calculated_total
FROM Orders
LIMIT 5;



-- Section 9: Transactions

-- Task 18: Write a transaction that updates a product's price and demonstrate the use of COMMIT and ROLLBACK.

-- Demonstration 1: Successful update committed
START TRANSACTION;
UPDATE Products 
SET price = 68000.00 
WHERE product_id = 1;
COMMIT;

-- Verification after commit
SELECT product_id, product_name, price FROM Products WHERE product_id = 1;

-- Demonstration 2: Temporary update rolled back
START TRANSACTION;
UPDATE Products 
SET price = 99999.00 
WHERE product_id = 1;
-- Undo the changes
ROLLBACK;

-- Verification after rollback
SELECT product_id, product_name, price FROM Products WHERE product_id = 1;



-- Section 10: SQL Triggers

-- Task 19: Create a trigger that automatically records new orders in an Order_Audit table whenever a new order is inserted.
CREATE TABLE IF NOT EXISTS Order_Audit (
    audit_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    customer_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    order_date DATE NOT NULL,
    audit_action VARCHAR(50) DEFAULT 'ORDER_INSERTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TRIGGER IF EXISTS trg_after_order_insert;

DELIMITER $$
CREATE TRIGGER trg_after_order_insert
AFTER INSERT ON Orders
FOR EACH ROW
BEGIN
    INSERT INTO Order_Audit (order_id, customer_id, product_id, quantity, order_date)
    VALUES (NEW.order_id, NEW.customer_id, NEW.product_id, NEW.quantity, NEW.order_date);
END$$
DELIMITER ;

-- Verification of Trigger:
INSERT INTO Orders (customer_id, product_id, quantity, order_date)
VALUES (1, 2, 1, CURRENT_DATE);

SELECT * FROM Order_Audit;



-- Section 11: Performance Tuning

-- Task 20: Optimize the given unoptimized query applying performance tuning best practices.
/*
SELECT DISTINCT *
FROM Products
WHERE YEAR(created_date) = 2024
AND category = 'Electronics';

Apply the following improvements:
1. Avoid SELECT *.
2. Remove unnecessary DISTINCT.
3. Avoid functions on indexed columns.
4. Use an index-friendly query.
*/

-- Optimization Step 1: Create composite index for index-friendly search
CREATE INDEX idx_products_cat_created ON Products(category, created_date);

-- Optimization Step 2: Optimized Query
SELECT 
    product_id,
    product_name,
    category,
    price,
    created_date
FROM Products
WHERE category = 'Electronics'
  AND created_date >= '2024-01-01' 
  AND created_date < '2025-01-01';

-- Performance Verification
EXPLAIN SELECT 
    product_id,
    product_name,
    category,
    price,
    created_date
FROM Products
WHERE category = 'Electronics'
  AND created_date >= '2024-01-01' 
  AND created_date < '2025-01-01';
