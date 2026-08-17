# Logistics Management System

## About the project

Logistics Management System is a Spring Boot REST API designed to support the core operations of a logistics company.

The application currently supports customer, shipment, courier, and vehicle management. Customers can be created, retrieved, updated, activated, deactivated, and deleted according to domain rules. Shipments can be created for existing customers, persisted in PostgreSQL, priced automatically based on weight, and moved through a controlled delivery lifecycle. Couriers can be created, retrieved, activated, deactivated, and moved between available and busy states according to domain rules. Vehicles can be created, retrieved, validated by registration number and maximum load, and moved between available, busy, and inactive states.
The shipment lifecycle currently supports the following states:

CREATED → READY_FOR_PICKUP → IN_TRANSIT → DELIVERED

Shipments can also be cancelled while they are in the CREATED or READY_FOR_PICKUP state.

Courier state transitions are also controlled by the domain model. Active and available couriers can be marked as busy, busy couriers can become available again, and only eligible couriers can be activated or deactivated.

The project includes request validation, domain-specific exceptions, centralized REST error handling, DTO-based API responses, automated tests across multiple application layers, and continuous integration with GitHub Actions.

This is my first Spring Boot project and an important part of my backend development portfolio. I develop it incrementally as I learn new backend technologies and architectural concepts.

The next major stages of the project will focus on shipment assignment workflows and further integration between shipments, couriers, and vehicles.
## Current features

### Customer management

- Create a new customer
- Retrieve a customer by ID
- Retrieve all customers
- Partially update customer data
- Update customer email with uniqueness validation
- Activate and deactivate customers
- Prevent deactivation when a customer has outstanding debt
- Delete customers only when they are inactive
- Validate customer and address data

### Shipment management

- Create shipments for existing customers
- Retrieve a shipment by ID
- Retrieve all shipments
- Store pickup and delivery addresses
- Automatically calculate shipment price based on weight
- Enforce shipment weight limits
- Manage shipment lifecycle transitions
- Mark shipments as ready for pickup
- Mark shipments as in transit
- Mark shipments as delivered
- Cancel eligible shipments
- Prevent invalid shipment status transitions
- Return dedicated `ShipmentResponse` DTOs instead of exposing JPA entities directly

### Courier management

- Create a new courier
- Retrieve a courier by ID
- Retrieve all couriers
- Store courier data in PostgreSQL
- Enforce unique courier phone numbers
- Activate and deactivate couriers
- Mark couriers as busy
- Mark busy couriers as available
- Prevent invalid courier state transitions
- Prevent busy couriers from being deactivated
- Return dedicated CourierResponse DTOs instead of exposing JPA entities directly
- Return 404 Not Found for missing couriers
- Return 409 Conflict for invalid courier state transitions

### Vehicle Management

- Creating vehicles
- Retrieving all vehicles
- Retrieving a vehicle by ID
- Unique registration number validation
- Maximum load validation
- Vehicle type support
- Vehicle availability and activity management
- Transactional status updates using JPA dirty checking

#### Vehicle statuses

Vehicles can have one of the following logical statuses:

- `AVAILABLE` — active and available
- `BUSY` — active but unavailable
- `INACTIVE` — inactive and unavailable

Supported status transitions:

- `AVAILABLE → BUSY`
- `BUSY → AVAILABLE`
- `AVAILABLE → INACTIVE`
- `INACTIVE → AVAILABLE`

### API and infrastructure

- Persist application data with PostgreSQL and Spring Data JPA
- Return structured API error responses
- Handle validation errors globally with `@RestControllerAdvice`
- Return appropriate HTTP status codes for invalid requests, conflicts, and missing resources
- Run automated domain, mapper, repository, service, and controller tests
- Build and test the application automatically with GitHub Actions

## Customer API endpoints

| Method   | Endpoint                                 | Description                    | Possible responses                                           |
|----------|------------------------------------------|--------------------------------|--------------------------------------------------------------|
| `GET`    | `/api/customers`                         | Retrieve all customers         | `200 OK`                                                     |
| `GET`    | `/api/customers/{customerId}`            | Retrieve a customer by ID      | `200 OK`, `404 Not Found`                                    |
| `POST`   | `/api/customers`                         | Create a new customer          | `201 Created`, `400 Bad Request`, `409 Conflict`             |
| `DELETE` | `/api/customers/{customerId}`            | Delete a customer by ID        | `204 No Content`, `404 Not Found`, `409 Conflict`            |
| `PATCH`  | `/api/customers/{customerId}`            | Partially update customer data | `200 OK`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| `PATCH` | `/api/customers/{customerId}/activate`   | Activate a customer            | `200 OK`, `404 Not Found`, `409 Conflict`|
| `PATCH` | `/api/customers/{customerId}/deactivate` | Deactivate a customer          | `200 OK`, `404 Not Found`, `409 Conflict`|

## Shipment API endpoints

| Method  | Endpoint                                      | Description                       | Possible responses                               |
|---------|-----------------------------------------------|-----------------------------------|--------------------------------------------------|
| `GET`   | `/api/shipments`                              | Retrieve all shipments            | `200 OK`                                         |
| `GET`   | `/api/shipments/{shipmentId}`                 | Retrieve a shipment by ID         | `200 OK`, `404 Not Found`                        |
| `POST`  | `/api/shipments`                              | Create a new shipment             | `201 Created`, `400 Bad Request`, `404 Not Found`|
| `PATCH` | `/api/shipments/{shipmentId}/ready-for-pickup`| Mark shipment as ready for pickup | `200 OK`, `404 Not Found`, `409 Conflict`        |
| `PATCH` | `/api/shipments/{shipmentId}/in-transit`      | Mark shipment as in transit       | `200 OK`, `404 Not Found`, `409 Conflict`        |
| `PATCH` | `/api/shipments/{shipmentId}/delivered`       | Mark shipment as delivered        | `200 OK`, `404 Not Found`, `409 Conflict`        |
| `PATCH` | `/api/shipments/{shipmentId}/cancelled`       | Cancel a shipment                 | `200 OK`, `404 Not Found`, `409 Conflict`        |

## Courier API endpoints

| Method  | Endpoint                               | Description                | Possible responses                               |
|---------|----------------------------------------|----------------------------|--------------------------------------------------|
| `GET`   | `/api/couriers`                        | Retrieve all couriers      | `200 OK`                                         |
| `GET`   | `/api/couriers/{courierId}`            | Retrieve a courier by ID   | `200 OK`, `404 Not Found`                        |
| `POST`  | `/api/couriers`                        | Create a new courier       | `201 Created`, `400 Bad Request`, `409 Conflict` |
| `PATCH` | `/api/couriers/{courierId}/busy`       | Mark courier as busy       | `200 OK`, `404 Not Found`, `409 Conflict`        |
| `PATCH` | `/api/couriers/{courierId}/available`  | Mark courier as available | `200 OK`, `404 Not Found`, `409 Conflict`        |
| `PATCH` | `/api/couriers/{courierId}/deactivate` | Deactivate a courier  | `200 OK`, `404 Not Found`, `409 Conflict`        |
| `PATCH` | `/api/couriers/{courierId}/activate`   | Activate a courier      | `200 OK`, `404 Not Found`, `409 Conflict`        |

## Vehicle API endpoints

| Method  | Endpoint                               | Description              | Possible responses                                          |
|---------|----------------------------------------|--------------------------|-------------------------------------------------------------|
| `GET`   | `/api/vehicles`                        | Retrieve all vehicles    | `200 OK`                                                    |
| `GET`   | `/api/vehicles/{vehicleId}`            | Retrieve a vehicle by ID | `200 OK`, `404 Not Found`                                   |
| `POST`  | `/api/vehicles`                        | Create a new vehicle     | `201 Created`, `400 Bad Request`, `409 Conflict`            |
| `PATCH` | `/api/vehicles/{vehicleId}/status`     | Update vehicle status    | `200 OK`, `404 Not Found`, `409 Conflict`, `400 Bad Request`|



## Technologies

- **Java 21** — application language
- **Spring Boot** — application configuration and runtime
- **Spring Web MVC** — REST controllers and HTTP request handling
- **Spring Data JPA** — database persistence and repository abstraction
- **Hibernate** — JPA implementation and ORM
- **PostgreSQL** — relational database used by the application
- **H2** — embedded database used in persistence tests
- **Jakarta Bean Validation** — request data validation
- **Maven** — dependency management and build automation
- **Lombok** — reduction of repetitive Java code
- **JUnit 5** — automated testing
- **Mockito** — mocking dependencies in unit and controller tests
- **MockMvc** — testing REST endpoints without starting a real server
- **GitHub Actions** — continuous integration and automated test execution

## Project structure

```text
src/main/java/com/mbugajski/logistics/
├── address/
│   ├── dto/
│   ├── entity/
│   └── repository/
├── common/
│   └── exception/
├── customer/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── exception/
│   ├── repository/
│   └── service/
├── courier/
│   ├── controller/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── entity/
│   ├── exception/
│   ├── mapper/
│   ├── repository/
│   └── service/
├── shipment/
│   ├── controller/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── entity/
│   ├── exception/
│   ├── mapper/
│   ├── repository/
│   └── service/
├── vehicle/
│   ├── controller/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── entity/
│   ├── exception/
│   ├── mapper/
│   ├── repository/
│   └── service/
└── LogisticsManagementSystemApplication.java
```
## Database

The application uses PostgreSQL as its main relational database.

The default configuration expects a database named:

```text
logistics_management_system
```

Database credentials are provided through environment variables:

```text
DB_USERNAME
DB_PASSWORD
```

The application connects to PostgreSQL using:


```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/logistics_management_system
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```


Make sure PostgreSQL is running and the database exists before starting the application.

For example, the database can be created with:
```sql
CREATE DATABASE logistics_management_system;
```

Repository tests use an embedded H2 database, so PostgreSQL is not required to run the automated test suite.

## Testing

The project includes automated tests for multiple application layers:

- domain model validation and behavior
- shipment lifecycle rules
- shipment price calculation
- repository persistence
- JPA dirty checking
- service logic and exception handling
- DTO mapping
- REST controller endpoints
- request validation
- global API error responses
- Spring application context startup
- courier domain state transition rules
- courier persistence and unique phone number constraints
- courier JPA dirty checking
- courier service behavior and exception handling
- courier REST endpoints
- vehicle domain validation and state transition tests
- vehicle repository persistence and unique registration number tests
- JPA dirty checking tests
- vehicle service tests
- vehicle REST controller and validation tests

Tests can be executed locally with Maven Wrapper:

```bash
./mvnw test
```

On Windows:

```cmd
mvnw.cmd test
```

Every pull request and push to the `main` branch is also verified automatically by GitHub Actions.

## Running the application

Before starting the application, make sure PostgreSQL is running,
the `logistics_management_system` database exists, and the
`DB_USERNAME` and `DB_PASSWORD` environment variables are configured.


### Requirements

- Java 21
- PostgreSQL
- Git

Maven does not need to be installed separately because the project includes Maven Wrapper.

### Clone the repository

```bash
git clone https://github.com/mbugajski3/spring-logistics-management-system.git
cd spring-logistics-management-system
```

### Run on Linux or macOS

```bash
./mvnw spring-boot:run
```

### Run on Windows

```cmd
mvnw.cmd spring-boot:run
```

The application starts by default at:

```text
http://localhost:8080
```

The APIs are available under:

```text
http://localhost:8080/api/customers
http://localhost:8080/api/shipments
http://localhost:8080/api/couriers
http://localhost:8080/api/vehicles
```

## Example requests

Ready-to-use request examples are available in [`http/customer-api.http`](http/customer-api.http) and can be executed directly with the IntelliJ IDEA HTTP Client.

### Create a customer

```http
POST http://localhost:8080/api/customers
Content-Type: application/json

{
  "firstName": "Anna",
  "lastName": "Kowalska",
  "email": "anna.kowalska@example.com",
  "phoneNumber": "500600700",
  "address": {
    "street": "Długa",
    "buildingNumber": "10A",
    "apartmentNumber": "5",
    "city": "Gdańsk",
    "postalCode": "80-831",
    "country": "Poland"
  }
}
```

### Create a shipment

```http
POST http://localhost:8080/api/shipments
Content-Type: application/json

{
  "customerId": 1,
  "pickupAddress": {
    "street": "Długa",
    "buildingNumber": "10",
    "apartmentNumber": "5",
    "city": "Gdańsk",
    "postalCode": "80-831",
    "country": "Poland"
  },
  "deliveryAddress": {
    "street": "Marszałkowska",
    "buildingNumber": "25",
    "apartmentNumber": "8",
    "city": "Warszawa",
    "postalCode": "00-001",
    "country": "Poland"
  },
  "weight": 7.00
}
```

### Create vehicle

```http
POST http://localhost:8080/api/vehicles
Content-Type: application/json

{
  "brand": "Ford",
  "model": "Transit",
  "registrationNumber": "GD 8032D",
  "vehicleType": "VAN",
  "maximumLoad": 120.00
}
```


### Example shipment created response:

```json
{
  "id": 1,
  "customerId": 1,
  "customerFirstName": "Anna",
  "customerLastName": "Kowalska",
  "pickupAddress": {
    "street": "Długa",
    "buildingNumber": "10",
    "apartmentNumber": "5",
    "city": "Gdańsk",
    "postalCode": "80-831",
    "country": "Poland"
  },
  "deliveryAddress": {
    "street": "Marszałkowska",
    "buildingNumber": "25",
    "apartmentNumber": "8",
    "city": "Warszawa",
    "postalCode": "00-001",
    "country": "Poland"
  },
  "weight": 7.00,
  "price": 25.00,
  "status": "CREATED",
  "createdAt": "2026-08-11T22:00:00"
}
```

### Example vehicle created response

```json
{
  "id": 1,
  "brand": "Ford",
  "model": "Transit",
  "registrationNumber": "GD 8032D",
  "vehicleType": "VAN",
  "maximumLoad": 120.00,
  "active": true,
  "available": true
}
```

### Update vehicle status

```http
PATCH http://localhost:8080/api/vehicles/1/status
Content-Type: application/json

{
  "status": "BUSY"
}
```
### Example update status response

```json
{
  "id": 1,
  "brand": "Ford",
  "model": "Transit",
  "registrationNumber": "GD 8032D",
  "vehicleType": "VAN",
  "maximumLoad": 120.00,
  "active": true,
  "available": false
}
```

## Roadmap

- [x] Add a customer update endpoint
- [x] Return `409 Conflict` when an email address is already in use
- [x] Replace the in-memory repository with PostgreSQL and Spring Data JPA
- [x] Add shipment creation and management
- [x] Add courier management
- [x] Add vehicle management
- [ ] Implement courier and vehicle assignment workflows
- [x] Calculate shipment prices based on weight
- [ ] Extend shipment pricing with additional delivery conditions
- [ ] Add Docker configuration
- [ ] Add OpenAPI documentation
- [ ] Create a simple frontend for interacting with the system

## Author

**Michał Bugajski**

GitHub: [@mbugajski3](https://github.com/mbugajski3)
