# Online Bookstore Database Assignment

## Assignment Overview
This assignment demonstrates foundational SQL concepts for managing an online bookstore database, including:
- Data Definition Language (DDL): Creating tables, specifying primary/foreign keys, and setting constraints (`PRIMARY KEY`, `NOT NULL`, `UNIQUE`, `DEFAULT`).
- Data Manipulation Language (DML): Inserting records, updating fields, and deleting rows.
- Data Retrieval & Filtering: Projections, aliases, `DISTINCT`, conditions with `WHERE`, `BETWEEN`, `LIKE`, and comparisons.
- Aggregation & Grouping: `COUNT`, `SUM`, `AVG`, `MAX`, `MIN`, `GROUP BY`, and `HAVING`.
- Joins: `INNER JOIN`, `LEFT JOIN`, and `RIGHT JOIN`.

---

## Database Schema

### 1. `Authors` Table
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `author_id` | `INT` | `PRIMARY KEY` | Unique identifier for author |
| `author_name` | `VARCHAR(100)` | - | Name of author |
| `country` | `VARCHAR(50)` | - | Country of residence/origin |

### 2. `Books` Table
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `book_id` | `INT` | `PRIMARY KEY` | Unique identifier for book |
| `title` | `VARCHAR(100)` | - | Title of book |
| `author_id` | `INT` | `FOREIGN KEY` | References `Authors(author_id)` |
| `category` | `VARCHAR(50)` | - | Book genre/category |
| `price` | `DECIMAL(10,2)` | - | Retail price of book |
| `published_year`| `INT` | - | Year of publication |

### 3. `Publishers` Table (Task 21)
| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `publisher_id` | `INT` | `PRIMARY KEY` | Unique identifier for publisher |
| `publisher_name`| `VARCHAR(100)`| `NOT NULL`, `UNIQUE` | Unique publisher name |
| `country` | `VARCHAR(50)` | `NOT NULL`, `DEFAULT 'India'` | Country of publisher |
| `established_year` | `INT` | `NOT NULL` | Year established |

---

## Steps to Execute the SQL Script

### Using MySQL CLI
1. Open your terminal and log into MySQL:
   ```bash
   mysql -u <username> -p
   ```
2. Execute the SQL file:
   ```sql
   SOURCE /Users/<username>/<path_to_folder>/bookstore.sql;
   ```

### Using MySQL Workbench / DBeaver / pgAdmin / GUI client
1. Open your SQL client and connect to your database instance.
2. Open [bookstore.sql](file:///Users/<username>/<path_to_folder>/bookstore.sql).
3. Execute the script (or run sections sequentially).