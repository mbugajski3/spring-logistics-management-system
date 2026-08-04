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
