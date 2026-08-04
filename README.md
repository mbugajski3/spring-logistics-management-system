# Logistics Management System

## About the project

Logistics Management System is a Spring Boot REST API designed to support the core operations of a logistics company.

The project currently focuses on customer management and provides endpoints for creating customers, retrieving a
customer by ID, retrieving all customers, and deleting customers. It also includes request validation, basic exception
handling, automated tests, and a continuous integration workflow.

This is my first Spring Boot project and an important part of my backend development portfolio. I plan to develop it
continuously as I learn new technologies and backend concepts. Each new stage of the project will reflect the skills I
have acquired and show my progress in building increasingly complex applications.

The long-term goal is to extend the system with shipment creation, delivery management, courier and vehicle assignments,
and shipment price calculation based on weight and other conditions. In the future, the project may also include a
simple frontend for interacting with the API.

## Current features

- Create a new customer
- Retrieve a customer by ID
- Retrieve all customers
- Delete a customer
- Validate customer and address data
- Return appropriate HTTP status codes for invalid requests and missing customers
- Run automated domain, repository, service, and controller tests
- Build and test the application automatically with GitHub Actions

## API endpoints

| Method | Endpoint | Description | Possible responses |
|---|---|---|---|
| `GET` | `/api/customers` | Retrieve all customers | `200 OK` |
| `GET` | `/api/customers/{customerId}` | Retrieve a customer by ID | `200 OK`, `404 Not Found` |
| `POST` | `/api/customers` | Create a new customer | `201 Created`, `400 Bad Request` |
| `DELETE` | `/api/customers/{customerId}` | Delete a customer by ID | `204 No Content`, `404 Not Found` |

## Technologies

- **Java 21** — application language
- **Spring Boot** — application configuration and runtime
- **Spring Web MVC** — REST controllers and HTTP request handling
- **Jakarta Bean Validation** — request data validation
- **Maven** — dependency management and build automation
- **Lombok** — reduction of repetitive Java code
- **JUnit 5** — automated testing
- **Mockito** — mocking dependencies in unit and controller tests
- **MockMvc** — testing REST endpoints without starting a real server
- **GitHub Actions** — continuous integration and automated test execution

## Project structure

```text
.
├── .github/
│   └── workflows/
│       └── ci.yml
├── .mvn/
├── http/
│   └── customer-api.http
├── src/
│   ├── main/
│   │   ├── java/com/mbugajski/logistics/
│   │   │   ├── LogisticsManagementSystemApplication.java
│   │   │   └── customer/
│   │   │       ├── Address.java
│   │   │       ├── CreateAddressRequest.java
│   │   │       ├── CreateCustomerRequest.java
│   │   │       ├── Customer.java
│   │   │       ├── CustomerController.java
│   │   │       ├── CustomerNotFoundException.java
│   │   │       ├── CustomerRepository.java
│   │   │       └── CustomerService.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/mbugajski/logistics/
├── .editorconfig
├── .gitattributes
├── .gitignore
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## Testing

The project includes automated tests for multiple application layers:

- domain model validation and behavior
- repository operations
- service logic and exception handling
- REST controller endpoints
- request validation
- Spring application context startup

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

### Requirements

- Java 21
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

The customer API is available under:

```text
http://localhost:8080/api/customers
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

## Roadmap

- [ ] Add a customer update endpoint
- [ ] Return `409 Conflict` when an email address is already in use
- [ ] Replace the in-memory repository with PostgreSQL and Spring Data JPA
- [ ] Add shipment creation and management
- [ ] Add courier and vehicle management
- [ ] Implement courier and vehicle assignment workflows
- [ ] Calculate shipment prices based on weight and delivery conditions
- [ ] Add Docker configuration
- [ ] Add OpenAPI documentation
- [ ] Create a simple frontend for interacting with the system

## Author

**Michał Bugajski**

GitHub: [@mbugajski3](https://github.com/mbugajski3)
