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

## Running the application

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

Resets all accounts.

**Request**

```http
POST /reset
```

**Response:** `200 OK`

### Get Account Balance

Retrieves the balance of an account.

**Request**

```http
GET /balance?account_id=100
```

**Success response:** `200 OK`

```json
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
    "balance": 0
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

## Tests

Run the complete test suite:

```bash
mvn clean test
```

The tests cover business logic and API integration, including deposits, withdrawals, transfers, insufficient funds, missing accounts, balance queries, and account reset.
