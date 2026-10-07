# Online Bookstore REST API

A Spring Boot RESTful API backend for an Online Bookstore System with thread-safe in-memory `ConcurrentHashMap` storage.

---

## 🛠️ Prerequisites

- **Java:** JDK 17 or higher
- **Build Tool:** Apache Maven 3.8+

---

## 🚀 Setup & Run Commands

### 1. Build and Run All Tests

```powershell
mvn clean test
```

### 2. Start the Application Server

```powershell
mvn spring-boot:run
```

The application will start on **`http://localhost:8080`**.

---

## 📋 API Endpoints

### 📚 Books API (`/v1/books`)

| Method   | Endpoint         | Description                                                           |
| :------- | :--------------- | :-------------------------------------------------------------------- |
| `GET`    | `/v1/books`      | Fetch all books (supports `?genre=...`, `?author=...`, `?search=...`) |
| `GET`    | `/v1/books/{id}` | Fetch details of a specific book by ID                                |
| `POST`   | `/v1/books`      | Add a new book to the inventory                                       |
| `PATCH`  | `/v1/books/{id}` | Update the price of an existing book                                  |
| `DELETE` | `/v1/books/{id}` | Delete a book record from inventory                                   |

---

### 🛒 Shopping Cart API (`/v1/cart`)

| Method   | Endpoint         | Description                          |
| :------- | :--------------- | :----------------------------------- |
| `POST`   | `/v1/cart/items` | Add a book item to the shopping cart |
| `GET`    | `/v1/cart`       | View current shopping cart           |
| `DELETE` | `/v1/cart`       | Clear current shopping cart          |

---

### 📦 Orders API (`/v1/orders`)

| Method | Endpoint          | Description                                |
| :----- | :---------------- | :----------------------------------------- |
| `POST` | `/v1/orders`      | Place a new book order                     |
| `GET`  | `/v1/orders/{id}` | Retrieve details of a specific order by ID |

---

### 👤 User Orders API (`/v1/users`)

| Method | Endpoint                    | Description                                      |
| :----- | :-------------------------- | :----------------------------------------------- |
| `GET`  | `/v1/users/{userId}/orders` | Retrieve all orders belonging to a specific user |

---
