# Spring Boot CRUD API — Products

A RESTful API built with Spring Boot for managing products, implementing full CRUD operations with pagination, sorting, filtering, partial updates, soft delete and audit timestamps.

---

## Table of Contents

- [Technologies](#technologies)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [API Documentation (Swagger)](#api-documentation-swagger)
- [API Endpoints](#api-endpoints)
  - [Get All Products](#get-all-products)
  - [Get Product by ID](#get-product-by-id)
  - [Create Product](#create-product)
  - [Update Product](#update-product)
  - [Partially Update Product](#partially-update-product)
  - [Delete Product](#delete-product)
- [Error Responses](#error-responses)
- [Running Tests](#running-tests)

---

## Technologies

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Programming language |
| Spring Boot | 3.3.5 | Application framework |
| Spring Web | — | REST API layer |
| Spring Data JPA | — | Data persistence and auditing |
| Spring Validation | — | Request body validation (`@Valid`) |
| Spring Boot Actuator | — | Health and info endpoints |
| SpringDoc OpenAPI | 2.5.0 | Swagger UI / API documentation |
| MySQL | 8.0 | Relational database |
| H2 | — | In-memory database (tests only) |
| Hibernate | 6.5.3 | ORM (via Spring Data JPA) |
| MapStruct | 1.6.3 | Object mapping (DTO ↔ Domain ↔ Entity) |
| Lombok | 1.18.36 | Boilerplate reduction |
| JUnit 5 | — | Unit and integration testing |
| Mockito | — | Mocking for unit tests |
| Maven | 3.9.x | Build tool |

---

## Architecture

The project follows **Hexagonal Architecture** (Ports & Adapters) with vertical slicing by entity. This enforces a strict dependency rule: the domain has no framework dependencies, and all infrastructure details are isolated in adapters.

```
domain         ←  pure Java, no Spring/JPA imports
application    ←  imports domain; Spring Data Commons (Pageable/Page) allowed
infrastructure ←  imports application + domain + Spring/JPA/Jakarta
```

### Dependency flow

```
ProductController  →  IProductUseCase (port in)
ProductService     →  IProductRepositoryPort (port out)
ProductPersistenceAdapter  →  IProductJpaRepository (Spring Data JPA)
```

---

## Project Structure

```
src/main/java/com/example/productsapi/
├── ProductsApiApplication.java
└── product/
    ├── domain/
    │   ├── Product.java                          # Pure domain POJO (includes audit timestamps)
    │   ├── ProductPatch.java                     # Partial update data object
    │   ├── ProductFilter.java                    # Filter criteria for GET all
    │   └── exception/
    │       ├── ProductNotFoundException.java
    │       ├── InvalidDataEntryException.java
    │       └── DuplicateProductNameException.java
    ├── application/
    │   ├── port/
    │   │   ├── in/
    │   │   │   └── IProductUseCase.java          # Input port (interface)
    │   │   └── out/
    │   │       └── IProductRepositoryPort.java   # Output port (interface)
    │   └── service/
    │       └── ProductService.java               # Business logic (@Transactional)
    └── infrastructure/
        ├── config/
        │   ├── JpaAuditingConfig.java            # @EnableJpaAuditing
        │   └── OpenApiConfig.java                # Swagger / OpenAPI metadata
        └── adapter/
            ├── in/
            │   └── web/
            │       ├── ProductController.java        # REST controller
            │       ├── GlobalExceptionHandler.java   # @ControllerAdvice
            │       ├── IProductWebMapper.java         # MapStruct (DTO ↔ Domain)
            │       └── dto/
            │           ├── CreateProductRequest.java   # POST body
            │           ├── UpdateProductRequest.java   # PUT body (all fields optional)
            │           ├── PatchProductRequest.java    # PATCH body (all fields optional)
            │           ├── ProductResponse.java        # Response for all read/write ops
            │           ├── PagedResponse.java          # Paginated list wrapper
            │           ├── ErrorResponse.java          # Standard error body
            │           └── ValidationErrorResponse.java
            └── out/
                └── persistence/
                    ├── ProductJpaEntity.java            # @Entity with audit + soft-delete fields
                    ├── IProductJpaRepository.java       # Spring Data JPA + JpaSpecificationExecutor
                    ├── IProductPersistenceMapper.java   # MapStruct (Entity ↔ Domain)
                    ├── ProductSpecification.java        # JPA Specification builder for filtering
                    └── ProductPersistenceAdapter.java   # Repository adapter

src/test/java/com/example/productsapi/
└── product/
    ├── application/service/
    │   └── ProductServiceTest.java              # Unit tests (Mockito)
    └── infrastructure/adapter/
        ├── in/web/
        │   └── ProductControllerTest.java       # HTTP layer tests (@WebMvcTest)
        └── out/persistence/
            └── ProductPersistenceAdapterTest.java  # Integration tests (@DataJpaTest + H2)
```

---

## Requirements

- **JDK 17** or higher
- **MySQL 8.0** running on `localhost:3306`
- **Maven 3.9.x** or higher

---

## Getting Started

Requires a MySQL 8.0 instance running on `localhost:3306`. The database (`products_db`) is created automatically on first startup.

**1. Clone the repository**

```bash
git clone <repository-url>
cd Spring_Boot_CRUD_API
```

**2. Configure credentials** (if different from defaults)

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/products_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
```

**3. Build the project**

```bash
./mvnw clean package -DskipTests
```

**4. Run the application**

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## API Documentation (Swagger)

Once the application is running, interactive API documentation is available at:

```
http://localhost:8080/swagger-ui.html
```

The raw OpenAPI spec (JSON) is at:

```
http://localhost:8080/api-docs
```

Health and info endpoints (via Actuator):

```
http://localhost:8080/actuator/health
http://localhost:8080/actuator/info
```

---

## API Endpoints

Base URL: `http://localhost:8080/api/v1/products`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/` | Get all products (paginated, sorted, filterable) |
| `GET` | `/{id}` | Get a product by UUID |
| `POST` | `/` | Create a new product |
| `PUT` | `/{id}` | Update an existing product (partial fields accepted) |
| `PATCH` | `/{id}` | Partially update an existing product |
| `DELETE` | `/{id}` | Soft-delete a product |

---

### Get All Products

Retrieves a paginated and sorted list of products. Supports optional filtering by name, price range and stock. Returns `200` with an empty list when no products exist.

```
GET /api/v1/products
```

**Query parameters** (all optional)

| Parameter | Type | Default | Description |
|---|---|---|---|
| `page` | `int` | `0` | Page number (0-based) |
| `size` | `int` | `10` | Page size (max `50`) |
| `sort` | `string` | `id,asc` | Sort field and direction |
| `name` | `string` | — | Partial name match (case-insensitive) |
| `minPrice` | `decimal` | — | Minimum base price (inclusive) |
| `maxPrice` | `decimal` | — | Maximum base price (inclusive) |
| `minStock` | `long` | — | Minimum stock (inclusive) |

**Example request**

```
GET /api/v1/products?page=0&size=5&sort=name,asc&name=pen&minPrice=100
```

**Responses**

| Status | Description |
|---|---|
| `200 OK` | Products retrieved successfully (empty list if none found) |

**Example response — 200 OK**

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "pencil",
      "description": "black pencil",
      "stock": 10,
      "basePrice": 200.00,
      "costPrice": 150.00,
      "createdAt": "2024-03-12T18:30:00",
      "updatedAt": null
    }
  ],
  "currentPage": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

### Get Product by ID

```
GET /api/v1/products/{id}
```

**Path parameters**

| Parameter | Type | Description |
|---|---|---|
| `id` | `UUID` | ID of the product to retrieve |

**Responses**

| Status | Description |
|---|---|
| `200 OK` | Product found |
| `400 Bad Request` | Invalid UUID format |
| `404 Not Found` | No product with the given ID |

**Example response — 200 OK**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "pencil",
  "description": "black pencil",
  "stock": 10,
  "basePrice": 200.00,
  "costPrice": 150.00,
  "createdAt": "2024-03-12T18:30:00",
  "updatedAt": null
}
```

---

### Create Product

```
POST /api/v1/products
```

**Request body**

```json
{
  "name": "pencil",
  "description": "black pencil",
  "stock": 10,
  "basePrice": 200.00,
  "costPrice": 150.00
}
```

**Field constraints**

| Field | Type | Constraints |
|---|---|---|
| `name` | `String` | Required, not blank, must be unique |
| `description` | `String` | Optional |
| `stock` | `Long` | Required, `>= 0` |
| `basePrice` | `BigDecimal` | Required, `> 0` |
| `costPrice` | `BigDecimal` | Required, `> 0` |

**Responses**

| Status | Description |
|---|---|
| `201 Created` | Product created successfully |
| `400 Bad Request` | Validation failed or invalid data |
| `409 Conflict` | A product with that name already exists |

**Example response — 201 Created**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "pencil",
  "description": "black pencil",
  "stock": 10,
  "basePrice": 200.00,
  "costPrice": 150.00,
  "createdAt": "2024-03-12T18:30:00",
  "updatedAt": null
}
```

---

### Update Product

Updates the product with the given ID. Only the fields included in the request body are changed — omitted or `null` fields are left unchanged.

```
PUT /api/v1/products/{id}
```

**Path parameters**

| Parameter | Type | Description |
|---|---|---|
| `id` | `UUID` | ID of the product to update |

**Request body** (all fields optional)

```json
{
  "name": "blue pencil",
  "description": "blue pencil HB",
  "stock": 25,
  "basePrice": 220.00,
  "costPrice": 160.00
}
```

**Field constraints** (applied only when the field is present)

| Field | Type | Constraints |
|---|---|---|
| `name` | `String` | Min length 1, must be unique |
| `description` | `String` | Min length 1 |
| `stock` | `Long` | `>= 0` |
| `basePrice` | `BigDecimal` | `> 0` |
| `costPrice` | `BigDecimal` | `> 0` |

**Responses**

| Status | Description |
|---|---|
| `200 OK` | Product updated successfully |
| `400 Bad Request` | Validation failed or invalid data |
| `404 Not Found` | No product with the given ID |
| `409 Conflict` | A product with that name already exists |

**Example response — 200 OK**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "blue pencil",
  "description": "blue pencil HB",
  "stock": 25,
  "basePrice": 220.00,
  "costPrice": 160.00,
  "createdAt": "2024-03-12T18:30:00",
  "updatedAt": "2024-03-12T19:15:00"
}
```

---

### Partially Update Product

Updates only the fields included in the request body. Any field omitted or sent as `null` is left unchanged.

```
PATCH /api/v1/products/{id}
```

**Path parameters**

| Parameter | Type | Description |
|---|---|---|
| `id` | `UUID` | ID of the product to patch |

**Request body** (all fields optional)

```json
{
  "stock": 99,
  "basePrice": 210.00
}
```

Same field constraints as [Update Product](#update-product).

**Responses**

| Status | Description |
|---|---|
| `200 OK` | Product patched successfully |
| `400 Bad Request` | Validation failed on a provided field |
| `404 Not Found` | No product with the given ID |
| `409 Conflict` | A product with that name already exists |

**Example response — 200 OK**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "blue pencil",
  "description": "blue pencil HB",
  "stock": 99,
  "basePrice": 210.00,
  "costPrice": 160.00,
  "createdAt": "2024-03-12T18:30:00",
  "updatedAt": "2024-03-12T19:20:00"
}
```

---

### Delete Product

Performs a **soft delete** — the product is not removed from the database but is marked with a `deletedAt` timestamp and excluded from all subsequent queries.

```
DELETE /api/v1/products/{id}
```

**Path parameters**

| Parameter | Type | Description |
|---|---|---|
| `id` | `UUID` | ID of the product to delete |

**Responses**

| Status | Description |
|---|---|
| `204 No Content` | Product deleted successfully |
| `404 Not Found` | No product with the given ID |

---

## Error Responses

All errors return a JSON body. There are two response formats depending on the type of error.

### Standard error

Returned for business errors (`404`, `409`, `400`, `500`).

```json
{
  "statusCode": "NOT_FOUND",
  "errorMessage": "Product with id 550e8400-e29b-41d4-a716-446655440000 not found!",
  "timestamp": "2024-03-12T18:30:00"
}
```

| Field | Type | Description |
|---|---|---|
| `statusCode` | `String` | HTTP status name |
| `errorMessage` | `String` | Human-readable error description |
| `timestamp` | `String` | ISO-8601 date-time of the error |

**Possible error messages**

| Status | Message |
|---|---|
| `404 Not Found` | `Product with id {id} not found!` |
| `409 Conflict` | `A product with the name '{name}' already exists!` |
| `400 Bad Request` | `Invalid data entry!` |
| `400 Bad Request` | `Invalid value '{value}' for parameter '{param}'` |
| `500 Internal Server Error` | `An unexpected error occurred` |

### Validation error

Returned when the request body fails field-level validation (`400 Bad Request`).

```json
{
  "statusCode": "BAD_REQUEST",
  "errorMessage": "Validation failed",
  "fieldErrors": [
    "Name is required",
    "Stock must be zero or greater",
    "Base price must be positive"
  ],
  "timestamp": "2024-03-12T18:30:00"
}
```

| Field | Type | Description |
|---|---|---|
| `statusCode` | `String` | HTTP status name |
| `errorMessage` | `String` | General error description |
| `fieldErrors` | `String[]` | List of per-field validation messages |
| `timestamp` | `String` | ISO-8601 date-time of the error |

---

## Running Tests

The project has **41 tests** across three test classes, each targeting a different layer.

```bash
./mvnw test                                          # run all tests
./mvnw test -Dtest=ProductServiceTest                # unit tests only
./mvnw test -Dtest=ProductControllerTest             # HTTP layer tests only
./mvnw test -Dtest=ProductPersistenceAdapterTest     # persistence tests only
./mvnw test -Dtest=ProductServiceTest#createProduct* # specific test group
```

| Test class | Type | Description |
|---|---|---|
| `ProductServiceTest` | Unit | Mocks `IProductRepositoryPort` with Mockito; tests all business logic paths including happy paths, not-found and duplicate-name scenarios |
| `ProductControllerTest` | Web layer | Uses `@WebMvcTest`; verifies HTTP status codes, response bodies and Bean Validation behaviour for each endpoint |
| `ProductPersistenceAdapterTest` | Integration | Uses `@DataJpaTest` with an in-memory H2 database; verifies real persistence operations including audit timestamps, filtering and soft delete behaviour |
