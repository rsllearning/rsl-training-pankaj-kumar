# Online Bookstore REST API - Architecture & Design Assignment

## Task 1: Backend Architecture & Request Lifecycle

### 1. The Lifecycle of an "Add to Cart" Request

When a user browses the bookstore and clicks **"Add to Cart"** on a book page, here is the complete step-by-step journey of that request from the browser to the backend server and back:

```mermaid
sequenceDiagram
    autonumber
    actor User as User / Browser
    participant DNS as DNS Resolver
    participant App as Spring Boot Backend (Embedded Tomcat)
    participant Memory as In-Memory Store (ConcurrentHashMap)

    User->>DNS: Look up api.bookstore.com
    DNS-->>User: Return IP address (e.g. 198.51.100.42)
    Note over User, App: TCP 3-Way Handshake + TLS 1.3 Handshake (Port 443 / 8080)
    User->>App: HTTP POST /v1/cart/items (with JSON body & JWT token)
    App->>App: Parse request, validate JWT token & deserialize JSON
    App->>Memory: Look up book & check available stock
    Memory-->>App: Book found & stock available
    App->>Memory: Atomically add/update item in user's cart
    Memory-->>App: Cart updated
    App-->>User: 200 OK with updated cart JSON
    Note over User: Browser updates cart badge icon & shows toast
```

#### Step-by-Step Breakdown:

#### 1. User Action & Request Creation (Browser)

- The user clicks **"Add to Cart"** for a book (e.g., Book ID: `bk_849201`, Quantity: `1`).
- The frontend JavaScript prepares an asynchronous HTTP `POST` request using `fetch()` or `axios`:
  - **URL:** `https://api.bookstore.com/v1/cart/items`
  - **Method:** `POST`
  - **Headers:** `Content-Type: application/json`, `Authorization: Bearer <jwt_token>`
  - **Body:** `{"bookId": "bk_849201", "quantity": 1}`

#### 2. DNS Lookup

Before sending any packets, the browser needs the server's IP address:

- It first checks its local browser DNS cache.
- If not found, it checks the OS cache and `hosts` file.
- If still not cached, it queries the recursive DNS resolver (like ISP DNS or `8.8.8.8`).
- The DNS resolver walks through Root, `.com` TLD, and authoritative nameservers to resolve `api.bookstore.com` to an IP address (e.g., `198.51.100.42`).

#### 3. Network Connection (TCP & TLS Handshake)

Once the IP is known, the browser opens a direct secure connection to the backend server:

- **TCP 3-Way Handshake:** Sends `SYN`, receives `SYN-ACK`, and replies with `ACK` to establish the connection.
- **TLS 1.3 Handshake:** The client and server agree on encryption ciphers, the server presents its SSL certificate, and both establish shared encryption keys for HTTPS.
- _Note on Ports:_ Plain HTTP uses **Port 80** (unencrypted), while HTTPS uses **Port 443** (or backend dev port like `8080`).

#### 4. Web Server & Dispatching (Spring Boot)

- Embedded Tomcat receives the raw HTTP request, parses the headers, and deserializes the JSON body into a Java DTO.
- Spring Security filter intercepts the request, validates the JWT token from the `Authorization` header, and extracts the `userId`.
- Spring's `DispatcherServlet` routes the request to the matching controller method: `CartController.addItemToCart()`.

#### 5. Business Logic & In-Memory Storage

- `CartController` passes the validated request to `CartService`.
- `CartService` checks `BookRepository` (`ConcurrentHashMap<String, Book>`) to ensure the book exists and has sufficient stock.
- It then updates the user's cart in `CartItemRepository` (`ConcurrentHashMap<String, Map<String, CartItem>>`).
- Thread-safe atomic operations ensure no race conditions occur when multiple requests access or modify inventory and cart state simultaneously.

#### 6. Response Construction & Transmission

- The server generates an HTTP `200 OK` response with the updated cart details (items, quantities, and subtotal) serialized as JSON in the response body.
- The response is sent back directly to the client browser over the established TCP/TLS connection.

#### 7. Client UI Update

- The browser receives the JSON response.
- The frontend updates its local cart state (React/Vue/vanilla state), updates the cart count badge in the navigation bar, and displays a feedback toast like _"Item added to cart"_.

---

### 2. Responsibilities of the Application / Business Logic Layer

The application layer sits between the HTTP controllers and the data store. Its core responsibilities include:

1. **Authentication & Identity Verification:**
   - Verifies the JWT signature and expiration.
   - Extracts the authenticated `userId` so users cannot modify someone else's cart or order.

2. **Input Validation:**
   - Checks that required fields are present (e.g., `bookId` is not blank, `quantity >= 1`).
   - Rejects invalid data early before touching the data layer.

3. **Inventory & Business Rules:**
   - Ensures the requested book exists and is active.
   - Verifies available stock before allowing an add-to-cart or checkout operation.

4. **Pricing Integrity:**
   - Always fetches current unit prices directly from the backend catalog. Prices sent by client requests are ignored to prevent tampering.
   - Calculates line item totals, taxes, discounts, and order subtotals.

5. **Concurrency & Thread Safety:**
   - Since we are using an in-memory `ConcurrentHashMap`, the service layer coordinates atomic operations (e.g. `compute()`, synchronized blocks) so two simultaneous requests do not corrupt inventory or cart state.

6. **Error Handling & Mapping:**
   - Catches business errors (like out of stock or book not found) and converts them into standard HTTP status codes (`404`, `409`, `422`) with clear JSON error messages.

---

## Task 2: RESTful Endpoint Design

### 1. Bookstore API Endpoints

| Requirement / Action                    | HTTP Method | Endpoint                    |  Safe?  | Idempotent? | Description                                                                                                                                                 |
| :-------------------------------------- | :---------: | :-------------------------- | :-----: | :---------: | :---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Fetch list of all books**             |    `GET`    | `/v1/books`                 | **Yes** |   **Yes**   | Returns a list of books in the catalog. Supports query filters like `?genre=fiction&author=Martin&page=1&limit=20`.                                         |
| **Fetch details of a specific book**    |    `GET`    | `/v1/books/{id}`            | **Yes** |   **Yes**   | Returns full details (title, author, price, stock) for a single book by its ID.                                                                             |
| **Add a new book to inventory**         |   `POST`    | `/v1/books`                 | **No**  |   **No**    | Creates a new book entry in the catalog. Returns `201 Created` with the new resource location in the `Location` header.                                     |
| **Update price of an existing book**    |   `PATCH`   | `/v1/books/{id}`            | **No**  |   **Yes**   | Updates specific fields (e.g. `{"price": 34.99}`) without replacing the entire book object. Setting a fixed value is idempotent.                            |
| **Delete a book from inventory**        |  `DELETE`   | `/v1/books/{id}`            | **No**  |   **Yes**   | Removes a book from inventory. Calling it again leaves the resource deleted (`404 Not Found` or `204 No Content`).                                          |
| **Place a new book order**              |   `POST`    | `/v1/orders`                | **No**  |   **No**    | Submits a new order, deducts inventory, and initiates payment. Repeated identical calls would create duplicate orders unless guarded by an idempotency key. |
| **Retrieve orders for a specific user** |    `GET`    | `/v1/users/{userId}/orders` | **Yes** |   **Yes**   | Fetches the list of previous orders and their fulfillment status for the given user ID.                                                                     |
| **Register a new user account**         |   `POST`    | `/v1/users`                 | **No**  |   **No**    | Creates a new user profile with account details (name, email, addresses). Returns `201 Created` with the new resource URI in the `Location` header.         |
| **Fetch details of a specific user**    |    `GET`    | `/v1/users/{userId}`        | **Yes** |   **Yes**   | Retrieves account and profile information (name, email, shipping address) for the specified user ID.                                                        |

---

## Task 3: Data Exchange & JSON Payloads

### Operation: Place a New Book Order (`POST /v1/orders`)

---

### 1. Successful Order Flow (`201 Created`)

#### Request Example

**Headers:**

```http
POST /v1/orders HTTP/1.1
Host: api.bookstore.com
Content-Type: application/json
Accept: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Idempotency-Key: 7b9c2a11-8f56-4c91-a62d-98e3b1c8f420
```

**Body (JSON):**

```json
{
  "customerId": "usr_908214",
  "items": [
    {
      "bookId": "bk_849201",
      "quantity": 2
    },
    {
      "bookId": "bk_110934",
      "quantity": 1
    }
  ],
  "shippingAddress": {
    "fullName": "Pankaj Kumar",
    "streetAddress": "456 Silicon Avenue, Suite 100",
    "city": "Bengaluru",
    "state": "Karnataka",
    "postalCode": "560100",
    "country": "India",
    "phoneNumber": "+919876543210"
  },
  "payment": {
    "paymentMethod": "CREDIT_CARD",
    "paymentToken": "tok_visa_4242_sec_998124"
  },
  "notes": "Please leave the parcel with security."
}
```

#### Response Example

**Headers:**

```http
HTTP/1.1 201 Created
Content-Type: application/json; charset=utf-8
Location: /v1/orders/ord_20261007_98421
Date: Wed, 07 Oct 2026 10:45:00 GMT
```

**Body (JSON):**

```json
{
  "orderId": "ord_20261007_98421",
  "orderNumber": "ORD-2026-88129",
  "customerId": "usr_908214",
  "orderStatus": "CONFIRMED",
  "paymentStatus": "PAID",
  "items": [
    {
      "bookId": "bk_849201",
      "title": "Designing Data-Intensive Applications",
      "quantity": 2,
      "unitPrice": 39.99,
      "lineTotal": 79.98
    },
    {
      "bookId": "bk_110934",
      "title": "Clean Architecture: A Craftsman's Guide",
      "quantity": 1,
      "unitPrice": 49.5,
      "lineTotal": 49.5
    }
  ],
  "pricing": {
    "subtotal": 129.48,
    "shippingFee": 5.0,
    "tax": 10.36,
    "total": 144.84,
    "currency": "USD"
  },
  "shippingAddress": {
    "fullName": "Pankaj Kumar",
    "streetAddress": "456 Silicon Avenue, Suite 100",
    "city": "Bengaluru",
    "state": "Karnataka",
    "postalCode": "560100",
    "country": "India"
  },
  "estimatedDelivery": "2026-10-12",
  "createdAt": "2026-10-07T10:45:00Z"
}
```

---

### 2. Error Handling Scenarios

#### Scenario A: Insufficient Stock (`409 Conflict`)

- **Situation:** The user tries to order `5` copies of `"Designing Data-Intensive Applications"`, but only `2` copies are left in stock.
- **Status Code:** `409 Conflict` (the request cannot be processed due to a conflict with current server inventory).

**Response:**

```http
HTTP/1.1 409 Conflict
Content-Type: application/json; charset=utf-8
```

```json
{
  "status": 409,
  "error": "INSUFFICIENT_STOCK",
  "message": "One or more items do not have enough inventory to complete your order.",
  "details": [
    {
      "bookId": "bk_849201",
      "title": "Designing Data-Intensive Applications",
      "requestedQuantity": 5,
      "availableStock": 2
    }
  ],
  "timestamp": "2026-10-07T10:45:01Z"
}
```

---

#### Scenario B: Validation Error (`422 Unprocessable Entity`)

- **Situation:** The request is missing the postal code in the shipping address, and the item quantity is set to a negative number (`-3`).
- **Status Code:** `422 Unprocessable Entity` (or `400 Bad Request`).

**Response:**

```http
HTTP/1.1 422 Unprocessable Entity
Content-Type: application/json; charset=utf-8
```

```json
{
  "status": 422,
  "error": "VALIDATION_FAILED",
  "message": "Invalid input data provided.",
  "fieldErrors": [
    {
      "field": "items[0].quantity",
      "rejectedValue": -3,
      "message": "Quantity must be at least 1."
    },
    {
      "field": "shippingAddress.postalCode",
      "rejectedValue": null,
      "message": "Postal code is required."
    }
  ],
  "timestamp": "2026-10-07T10:45:02Z"
}
```
