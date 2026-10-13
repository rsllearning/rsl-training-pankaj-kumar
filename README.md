# Leave Management API

## Project Overview
A RESTful API built with Spring Boot to manage employee leave requests. It provides full CRUD operations, input validation, date-range checks, centralized error handling, and a thread-safe in-memory store.

---

## Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 4.x / Spring Framework 7
- **Build Tool**: Gradle 9.7
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@FutureOrPresent`, `@Size`)
- **Testing**: JUnit 5, Mockito, MockMvc, AssertJ

---

## Features
- **Leave Request Management**: Full CRUD operations for employee leave requests.
- **In-Memory Thread-Safe Store**: `ConcurrentHashMap` repository avoiding database setup while maintaining ACID safety for concurrent writes.
- **Constructor Injection**: Recommended modern Spring pattern across all components.
- **Bean Validation**: Strict validations on inputs with meaningful error responses.
- **Centralized Error Handling**: Standardized error response structure with timestamp, HTTP status, and detailed error messages.
- **Spring Profile Support**: `dev` profile pre-loads sample data via `DataInitializerConfig` for immediate API exploration.

---

## Getting Started

### Prerequisites
- JDK 21 installed (`java -version`)
- Terminal or Bash shell

### Build and Run
Clone or navigate to the repository directory and navigate into `leave-management-api`:

```bash
cd leave-management-api
```

1. **Build the application**:
   ```bash
   ./gradlew clean build
   ```

2. **Run the application (with default active profile `dev`)**:
   ```bash
   ./gradlew bootRun
   ```

   The application will start on port `8080` with context path `/api`: `http://localhost:8080/api`.

3. **Run with a specific profile**:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

---

## API Endpoints Summary

Base URL: `http://localhost:8080/api`

| Method | Endpoint | Description | Status Code |
|---|---|---|---|
| `GET` | `/v1/leave` | Retrieve all leave requests | `200 OK` |
| `GET` | `/v1/leaves/{id}` | Retrieve a leave request by ID | `200 OK` / `404 Not Found` |
| `POST` | `/v1/leaves` | Create a new leave request | `201 Created` / `400 Bad Request` |
| `PUT` | `/v1/leaves/{id}` | Update an existing leave request | `200 OK` / `404 Not Found` / `400 Bad Request` |
| `DELETE` | `/v1/leaves/{id}` | Delete a leave request | `204 No Content` / `404 Not Found` |

---

## Example Requests & Responses

### 1. Create a Leave Request (`POST /api/v1/leaves`)
**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/leaves \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 105,
    "leaveType": "CASUAL",
    "startDate": "2026-10-20",
    "endDate": "2026-10-22",
    "reason": "Personal family event"
  }'
```

**Response (`201 Created`):**
```json
{
  "id": 3,
  "employeeId": 105,
  "leaveType": "CASUAL",
  "startDate": "2026-10-20",
  "endDate": "2026-10-22",
  "reason": "Personal family event",
  "status": "PENDING"
}
```

### 2. Retrieve All Leaves (`GET /api/v1/leave`)
**Request:**
```bash
curl http://localhost:8080/api/v1/leave
```

**Response (`200 OK`):**
```json
[
  {
    "id": 1,
    "employeeId": 101,
    "leaveType": "ANNUAL",
    "startDate": "2026-10-14",
    "endDate": "2026-10-17",
    "reason": "Annual family vacation",
    "status": "APPROVED"
  },
  {
    "id": 2,
    "employeeId": 102,
    "leaveType": "SICK",
    "startDate": "2026-10-13",
    "endDate": "2026-10-15",
    "reason": "Viral fever recovery",
    "status": "PENDING"
  }
]
```

### 3. Retrieve Leave by ID (`GET /api/v1/leaves/{id}`)
**Request:**
```bash
curl http://localhost:8080/api/v1/leaves/1
```

### 4. Update a Leave Request (`PUT /api/v1/leaves/{id}`)
**Request:**
```bash
curl -X PUT http://localhost:8080/api/v1/leaves/1 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "APPROVED",
    "reason": "Approved by manager"
  }'
```

### 5. Delete a Leave Request (`DELETE /api/v1/leaves/{id}`)
**Request:**
```bash
curl -X DELETE http://localhost:8080/api/v1/leaves/1
```

**Response:** `204 No Content`

### 6. Validation Error Example
**Request with invalid date range (`endDate` < `startDate`):**
```bash
curl -X POST http://localhost:8080/api/v1/leaves \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 105,
    "leaveType": "CASUAL",
    "startDate": "2026-10-25",
    "endDate": "2026-10-20",
    "reason": "Trip"
  }'
```

**Response (`400 Bad Request`):**
```json
{
  "timestamp": "2026-10-12T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "End date (2026-10-20) cannot be before start date (2026-10-25)",
  "path": "/api/v1/leaves",
  "details": [
    "End date (2026-10-20) cannot be before start date (2026-10-25)"
  ]
}
```

---

## Testing Instructions

Execute the entire automated test suite:
```bash
./gradlew test
```