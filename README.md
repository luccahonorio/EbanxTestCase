# EBANX Backend Challenge

REST API developed in Java with Spring Boot to manage account balances and financial events.

## Technologies

- Java 21
- Spring Boot 3
- Maven
- JUnit 5
- MockMvc

## Requirements

- Java 21+
- Maven 3.9+

## Running the Application

Clone the repository:

```bash
git clone https://github.com/luccahonorio/EbanxTestCase.git
cd EbanxTestCase
```

Run the application:

```bash
mvn spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

## API Endpoints

### Reset

Resets all accounts and clears the application state.

**Request**

```http
POST /reset
```

**Response:** `200 OK`

```text
OK
```

### Get Account Balance

Retrieves the balance of an account.

**Request**

```http
GET /balance?account_id=100
```

**Success response:** `200 OK`

```text
100
```

**Account not found:** `404 Not Found`

```text
0
```

### Process Events

Processes deposit, withdrawal, and transfer operations.

**Request**

```http
POST /event
Content-Type: application/json
```

#### Deposit

Creates an account if it does not exist or adds the amount to an existing account.

**Request body**

```json
{
  "type": "deposit",
  "destination": "100",
  "amount": 10
}
```

**Response:** `201 Created`

```json
{
  "destination": {
    "id": "100",
    "balance": 10
  }
}
```

#### Withdraw

Withdraws an amount from an existing account.

Assuming account `100` has an initial balance of `10`:

**Request body**

```json
{
  "type": "withdraw",
  "origin": "100",
  "amount": 5
}
```

**Response:** `201 Created`

```json
{
  "origin": {
    "id": "100",
    "balance": 5
  }
}
```

#### Transfer

Transfers an amount from an existing origin account to a destination account. The destination account is created if it does not exist.

Assuming account `100` has a balance of `10`:

**Request body**

```json
{
  "type": "transfer",
  "origin": "100",
  "amount": 5,
  "destination": "200"
}
```

**Response:** `201 Created`

```json
{
  "origin": {
    "id": "100",
    "balance": 5
  },
  "destination": {
    "id": "200",
    "balance": 5
  }
}
```

## Business Rules

- Deposits create an account if it does not exist.
- Withdrawals require an existing account.
- Transfers require an existing origin account and create the destination account if necessary.
- Deposit, withdrawal, and transfer amounts must be greater than zero.
- Accounts cannot have a negative balance.
- Account balance queries do not create or modify accounts.
- Reset clears all accounts.
- Invalid operations and insufficient funds return appropriate HTTP errors.

## Implementation Decisions

- **Layered architecture:** Controllers handle HTTP requests and responses, services contain business rules, and repositories manage account storage.
- **In-memory storage:** A `ConcurrentHashMap` is used to store accounts, keeping the implementation simple and avoiding unnecessary database infrastructure.
- **Concurrency:** Synchronized service operations protect balance updates and transfers from concurrent modifications within the application instance.
- **DTOs:** Request and response records separate the API contract from the domain model.
- **Exception handling:** A centralized `@RestControllerAdvice` translates business exceptions into HTTP responses.

## Project Structure

The application follows a layered architecture:

```text
src/
├── main/
│   ├── java/com/company/app/
│   │   ├── controller/       # HTTP endpoints
│   │   ├── domain/           # Domain models and enums
│   │   ├── dto/
│   │   │   ├── request/      # Request DTOs
│   │   │   └── response/     # Response DTOs
│   │   ├── exception/        # Centralized exception handling
│   │   ├── repository/       # Account storage
│   │   └── service/          # Business logic
│   └── resources/
└── test/
    └── java/com/company/app/
        ├── controller/       # API integration tests
        └── service/          # Business logic tests
```

## Error Handling

The API uses appropriate HTTP status codes to represent unsuccessful operations:

| Scenario | HTTP Status | Response |
|---|---:|---|
| Account not found | 404 | `0` |
| Insufficient funds | 400 | `{"error":"Insufficient funds"}` |
| Invalid amount (zero or negative) | 400 | Bad Request |

## Tests

The project includes unit tests for business logic and integration tests for REST API endpoints.

### Running Tests

Run the complete test suite using Maven:

```bash
mvn clean test
```

To run tests without cleaning the previous build:

```bash
mvn test
```

### Test Coverage

The test suite covers:

- Account creation through deposits.
- Multiple deposits into the same account.
- Successful withdrawals.
- Withdrawals with insufficient funds.
- Withdrawals from non-existing accounts.
- Withdrawals with zero or negative amounts.
- Withdrawals of the exact available balance.
- Transfers between existing accounts.
- Transfers to non-existing destination accounts.
- Transfers with insufficient funds.
- Transfers from non-existing accounts.
- Transfers with zero or negative amounts.
- Balance inquiries for existing and non-existing accounts.
- Verification that invalid operations do not modify account balances.
- Account reset.
- API integration and business logic validation.