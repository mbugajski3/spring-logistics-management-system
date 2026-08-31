# Logistics Management System

## About the project

Logistics Management System is a Spring Boot REST API designed to support the core operations of a logistics company.
The application currently supports customer, shipment, courier, vehicle, and shipment assignment management.
Customers can be created, retrieved, updated, activated, deactivated, and deleted according to domain rules.

Shipments can be created for existing customers, persisted in PostgreSQL, priced automatically based on weight, and moved through a controlled delivery lifecycle. Couriers and vehicles have controlled availability states and can be assigned to shipments through a transactional assignment workflow.

Shipment assignments connect a shipment with an available courier and vehicle. The assignment process validates the shipment state and vehicle load capacity, marks the selected courier and vehicle as busy, moves the shipment to `READY_FOR_PICKUP`, and persists the assignment as one atomic database transaction.

Assignments have their own lifecycle and can be `ACTIVE`, `COMPLETED`, `CANCELLED`, or `REASSIGNED`. When a shipment is delivered or cancelled, the active assignment is completed or cancelled and its courier and vehicle are released.

Shipments in the `READY_FOR_PICKUP` state can also be reassigned. Reassignment preserves the previous assignment record, releases its courier and vehicle, automatically selects an available courier and the smallest available vehicle with sufficient capacity, and creates a new active assignment. The whole operation is transactional to prevent partial state changes.
The shipment lifecycle currently supports the following states:

CREATED → READY_FOR_PICKUP → IN_TRANSIT → DELIVERED

Shipments can also be cancelled while they are in the CREATED or READY_FOR_PICKUP state.

Courier state transitions are also controlled by the domain model. Active and available couriers can be marked as busy, busy couriers can become available again, and only eligible couriers can be activated or deactivated.

The project includes request validation, domain-specific exceptions, centralized REST error handling, DTO-based API responses, automated tests across multiple application layers, and continuous integration with GitHub Actions.

This is my first Spring Boot project and an important part of my backend development portfolio. I develop it incrementally as I learn new backend technologies and architectural concepts.

Assignment history can be retrieved through the API, allowing clients to inspect previous reassigned, completed, cancelled, and active assignments in chronological order.

The next major stages of the project will focus on concurrency control, automatic shipment dispatching, and further logistics workflow automation.

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
- Retrieve shipments using paginated responses
- Filter shipments by status
- Filter shipments by customer ID
- Combine shipment filters using AND conditions
- Sort shipments by creation date, price, or weight
- Support ascending and descending sorting
- Limit shipment page size to a maximum of 100 records
- Return dedicated `ShipmentPaginationResponse` DTOs with pagination metadata
- Store pickup and delivery addresses
- Automatically calculate shipment price based on weight
- Enforce shipment weight limits
- Manage shipment lifecycle transitions through business operations
- Move shipments to `READY_FOR_PICKUP` as part of assignment
- Confirm shipment pickup only when an active assignment exists
- Confirm shipment delivery and complete the active assignment
- Cancel eligible shipments and release assigned resources
- Prevent invalid shipment status transitions
- Support shipment reassignment before pickup
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

### Vehicle management

- Create vehicles
- Retrieve all vehicles
- Retrieve a vehicle by ID
- Enforce unique registration numbers
- Validate maximum load
- Support multiple vehicle types
- Manage vehicle availability and activity
- Perform transactional status updates using JPA dirty checking

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

### Shipment assignment
- Assign a shipment to a courier and vehicle
- Allow initial assignments only for shipments in the `CREATED` state
- Require the selected courier to be active and available
- Require the selected vehicle to be active and available
- Validate vehicle maximum load against shipment weight
- Mark assigned courier and vehicle as busy
- Automatically move the shipment from `CREATED` to `READY_FOR_PICKUP`
- Track assignment lifecycle using `ACTIVE`, `COMPLETED`, `CANCELLED`, and `REASSIGNED`
- Store assignment creation and completion timestamps
- Require an active assignment before confirming shipment pickup
- Complete the active assignment after shipment delivery
- Cancel the active assignment when an assigned shipment is cancelled
- Release courier and vehicle resources after assignment completion or cancellation
- Reassign shipments while they are in `READY_FOR_PICKUP`
- Preserve previous assignment records after reassignment
- Automatically select an available courier during reassignment
- Automatically select the smallest available vehicle with sufficient load capacity
- Keep exactly one active assignment after successful reassignment
- Persist shipment, courier, vehicle, and assignment state changes transactionally
- Roll back the entire operation if any part of the workflow fails
- Return dedicated `ShipmentAssignmentResponse` DTOs
- Retrieve complete assignment history for a shipment
- Return assignment history chronologically by `assignedAt`
- Return an empty history for shipments that have never been assigned
- Preserve assignment status and lifecycle timestamps in history responses

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
| `GET`   | `/api/shipments`                              | Retrieve, paginate, filter, and sort shipments | `200 OK`, `400 Bad Request` |
| `GET`   | `/api/shipments/{shipmentId}`                 | Retrieve a shipment by ID         | `200 OK`, `404 Not Found`                        |
| `POST`  | `/api/shipments`                              | Create a new shipment             | `201 Created`, `400 Bad Request`, `404 Not Found`|
| `PATCH` | `/api/shipments/{shipmentId}/confirm-pickup`  | Confirm shipment pickup           | `200 OK`, `404 Not Found`, `409 Conflict`        |
| `PATCH` | `/api/shipments/{shipmentId}/confirm-delivery`| Confirm shipment delivery         | `200 OK`, `404 Not Found`, `409 Conflict`        |
| `PATCH` | `/api/shipments/{shipmentId}/cancel`          | Cancel a shipment                 | `200 OK`, `404 Not Found`, `409 Conflict`        |

### Shipment listing query parameters

`GET /api/shipments` supports pagination, filtering, and sorting.

| Parameter | Required | Default | Description |
|---|---|---|---|
| `page` | No | `0` | Zero-based page number |
| `size` | No | `20` | Number of shipments per page, maximum `100` |
| `status` | No | — | Filter by shipment status |
| `customerId` | No | — | Filter by customer ID |
| `sortBy` | No | — | Sort by `createdAt`, `price`, or `weight` |
| `direction` | No | — | Sort direction: `asc` or `desc` |

`sortBy` and `direction` must be provided together.
Filters can be combined and are applied using AND semantics.

Example:

```http
GET http://localhost:8080/api/shipments?status=CREATED&customerId=1&page=0&size=10&sortBy=createdAt&direction=desc
```

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

## Shipment Assignment API endpoints

| Method  | Endpoint                                  | Description                              | Possible responses                                            |
|---------|-------------------------------------------|------------------------------------------|---------------------------------------------------------------|
| `POST`  | `/api/shipments/{shipmentId}/assignment` | Assign a courier and vehicle to shipment | `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| `PATCH` | `/api/shipments/{shipmentId}/reassign`   | Reassign shipment to new resources       | `200 OK`, `404 Not Found`, `409 Conflict`                     |
| `GET`   | `/api/shipments/{shipmentId}/assignment-history` | Retrieve shipment assignment history | `200 OK`, `404 Not Found` |

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
│   ├── service/
│   └── specification/
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
├── assignment/
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
- shipment assignment domain tests
- shipment assignment service unit tests
- shipment assignment REST controller tests
- shipment assignment request validation tests
- transactional shipment assignment integration tests
- transaction rollback verification when assignment fails
- persistence verification across shipment, courier, vehicle, and assignment state changes
- shipment pagination service and controller tests
- shipment filtering unit and integration tests
- shipment sorting unit and integration tests
- combined shipment filtering and sorting tests
- pagination metadata verification
- invalid pagination and sorting parameter handling
- shipment assignment lifecycle tests
- shipment reassignment entity and service tests
- shipment reassignment REST controller tests
- reassignment persistence integration tests
- assignment history preservation verification
- courier and vehicle resource switching verification
- suitable vehicle selection based on shipment weight
- shipment assignment history service tests
- shipment assignment history REST controller tests
- empty assignment history behavior tests
- shipment assignment history persistence integration tests
- repository filtering by shipment ID
- assignment history ordering by `assignedAt`

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
http://localhost:8080/api/shipments/{shipmentId}/assignment
http://localhost:8080/api/shipments/{shipmentId}/reassign
http://localhost:8080/api/shipments/{shipmentId}/assignment-history
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
### Example customer created response

```json
{
  "id": 1,
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
  },
  "debt": 0.00,
  "active": true
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

### Create a courier

```http
POST http://localhost:8080/api/couriers
Content-Type: application/json

{
  "firstName": "Piotr",
  "lastName": "Nowak",
  "phoneNumber": "600700800"
}
```

### Example courier created response

```json
{
    "id": 1,
    "firstName": "Piotr",
    "lastName": "Nowak",
    "phoneNumber": "600700800",
    "active": true,
    "available": true
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

### Assign a shipment

```http
POST http://localhost:8080/api/shipments/1/assignment
Content-Type: application/json

{
  "courierId": 1,
  "vehicleId": 1
}
```

### Example shipment assignment response

```json
{
  "assignmentId": 1,
  "shipmentId": 1,
  "courierId": 1,
  "vehicleId": 1,
  "status": "ACTIVE",
  "assignedAt": "2026-08-28T16:00:00",
  "finishedAt": null
}
```

### Reassign a shipment

```http
PATCH http://localhost:8080/api/shipments/1/reassign
```

### Example shipment reassignment response

```json
{
  "assignmentId": 2,
  "shipmentId": 1,
  "courierId": 2,
  "vehicleId": 2,
  "status": "ACTIVE",
  "assignedAt": "2026-08-28T16:15:00",
  "finishedAt": null
}

```
### Reassignment behavior

Reassignment is available only while the shipment is in the `READY_FOR_PICKUP` state.

The previous assignment is marked as `REASSIGNED` and its resources are released. The system then selects an available courier and the smallest available vehicle with sufficient capacity and creates a new `ACTIVE` assignment.

### Get shipment assignment history

```http
GET http://localhost:8080/api/shipments/1/assignment-history
```

### Example assignment history response

```json
[
  {
    "assignmentId": 1,
    "shipmentId": 1,
    "courierId": 1,
    "vehicleId": 1,
    "status": "REASSIGNED",
    "assignedAt": "2026-08-31T10:00:00",
    "finishedAt": "2026-08-31T12:00:00"
  },
  {
    "assignmentId": 2,
    "shipmentId": 1,
    "courierId": 2,
    "vehicleId": 2,
    "status": "ACTIVE",
    "assignedAt": "2026-08-31T12:00:00",
    "finishedAt": null
  }
]
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
- [x] Calculate shipment prices based on weight
- [x] Add shipment assignment workflow
- [x] Add transactional assignment of couriers and vehicles
- [x] Validate vehicle capacity during assignment
- [x] Verify transaction rollback with integration tests
- [ ] Extend shipment pricing with additional delivery conditions
- [ ] Add Docker configuration
- [ ] Add OpenAPI documentation
- [ ] Create a simple frontend for interacting with the system
- [x] Add shipment reassignment
- [x] Preserve assignment history during reassignment
- [x] Add shipment assignment history retrieval endpoint
- [x] Release courier and vehicle after delivery or cancellation
- [x] Add pagination and filtering for large datasets
- [ ] Handle concurrent shipment assignments
- [ ] Add locking strategy for shared resources
- [ ] Introduce automatic shipment dispatching

## Author

**Michał Bugajski**

GitHub: [@mbugajski3](https://github.com/mbugajski3)
