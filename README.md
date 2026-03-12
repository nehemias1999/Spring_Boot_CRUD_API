# Spring Boot CRUD API — Products

A RESTful API built with Spring Boot for managing products, implementing full CRUD operations with pagination, sorting, partial updates and audit timestamps.

---

## Table of Contents

- [Technologies](#technologies)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
  - [Running with Docker Compose (recommended)](#running-with-docker-compose-recommended)
  - [Running locally](#running-locally)
- [Spring Profiles](#spring-profiles)
- [Environment Variables](#environment-variables)
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
| Spring Boot | 3.1.3 | Application framework |
| Spring Web | — | REST API layer |
| Spring Data JPA | 3.1.3 | Data persistence and auditing |
| Spring Validation | — | Request body validation (`@Valid`) |
| SpringDoc OpenAPI | 2.2.0 | Swagger UI / API documentation |
| MySQL | 8.0 | Relational database |
| H2 | — | In-memory database (tests only) |
| Hibernate | — | ORM (via Spring Data JPA) |
| MapStruct | 1.5.5.Final | Object mapping (DTO ↔ Domain ↔ Entity) |
| Lombok | 1.18.36 | Boilerplate reduction |
| JUnit 5 | — | Unit and integration testing |
| Mockito | — | Mocking for unit tests |
| Docker | — | Containerization |
| Docker Compose | — | Multi-container orchestration |
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
    │   └── exception/
    │       ├── ProductNotFoundException.java
    │       ├── EmptyProductsListException.java
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
            │           ├── UpdateProductRequest.java   # PUT body
            │           ├── PatchProductRequest.java    # PATCH body (all fields optional)
            │           ├── ProductResponse.java        # Response for all read/write ops
            │           ├── PagedResponse.java          # Paginated list wrapper
            │           ├── ErrorResponse.java          # Standard error body
            │           └── ValidationErrorResponse.java
            └── out/
                └── persistence/
                    ├── ProductJpaEntity.java            # @Entity with audit fields
                    ├── IProductJpaRepository.java       # Spring Data JPA
                    ├── IProductPersistenceMapper.java   # MapStruct (Entity ↔ Domain)
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
- **Maven 3.9.x** or higher
- **Docker 20.x** or higher
- **Docker Compose 1.29** or higher

---

## Getting Started

### Running with Docker Compose (recommended)

This method starts both the API and the MySQL database as containers — no local database setup needed. The API runs with the **`prod` profile** automatically.

**1. Clone the repository**

```bash
git clone <repository-url>
cd Spring_Boot_CRUD_API
```

**2. Review the `.env` file** in the project root. The default values work out of the box:

```env
MYSQL_CONTAINER_NAME=mysqldb
MYSQL_CONTAINER_PORT=3307
MYSQL_PORT=3306
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=products_db
MYSQL_USER=admin
MYSQL_PASSWORD=admin123
API_CONTAINER_NAME=products-api
API_CONTAINER_PORT=8080
API_PORT=8080
```

**3. Build and start the containers**

```bash
docker-compose up -d
```

**4. Check that both containers are running**

```bash
docker ps
```

You should see `products-api` and `mysqldb` with status `Up`.

**5. The API is available at**

```
http://localhost:8080
```

**6. View logs**

```bash
docker logs products-api -f
```

**7. Stop and remove containers**

```bash
docker-compose down
```

> To also remove the persisted database volume:
> ```bash
> docker-compose down -v
> ```

---

### Running locally

Requires a MySQL 8.0 instance running on `localhost:3306` with a database named `products_db`.

**1. Override the datasource URL**

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/products_db
spring.datasource.username=<your_user>
spring.datasource.password=<your_password>
```

**2. Build the project**

```bash
./mvnw clean package -DskipTests
```

**3. Run the application**

```bash
# Default profile (no extra logging)
./mvnw spring-boot:run

# Dev profile (SQL logging + DEBUG output)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API will be available at `http://localhost:8080`.

---

## Spring Profiles

The application ships with three configuration files. Activate a profile by setting `--spring.profiles.active=<profile>` (or `SPRING_PROFILES_ACTIVE` environment variable).

| Profile | File | When to use |
|---|---|---|
| _(default)_ | `application.properties` | Shared base config — datasource, JPA dialect, Jackson, Swagger paths |
| `dev` | `application-dev.properties` | Local development — `show-sql=true`, formatted SQL, DEBUG logging |
| `prod` | `application-prod.properties` | Production / Docker — `show-sql=false`, INFO/WARN logging only |

> Docker Compose automatically activates the `prod` profile via `SPRING_PROFILES_ACTIVE=prod`.

---

## Environment Variables

All environment variables are defined in the `.env` file at the project root and consumed by `docker-compose.yaml`.

| Variable | Default | Description |
|---|---|---|
| `MYSQL_CONTAINER_NAME` | `mysqldb` | MySQL container name (also the hostname inside the Docker network) |
| `MYSQL_CONTAINER_PORT` | `3307` | MySQL port exposed on the host machine |
| `MYSQL_PORT` | `3306` | MySQL port inside the container |
| `MYSQL_ROOT_PASSWORD` | `root` | MySQL root password |
| `MYSQL_DATABASE` | `products_db` | Database name |
| `MYSQL_USER` | `admin` | Application database user |
| `MYSQL_PASSWORD` | `admin123` | Application database password |
| `API_CONTAINER_NAME` | `products-api` | API container name |
| `API_CONTAINER_PORT` | `8080` | API port exposed on the host machine |
| `API_PORT` | `8080` | API port inside the container |

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

---

## API Endpoints

Base URL: `http://localhost:8080/api/v1/products`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/getAll` | Get all products (paginated & sorted) |
| `GET` | `/get/{id}` | Get a product by ID |
| `POST` | `/create` | Create a new product |
| `PUT` | `/update/{id}` | Fully replace an existing product |
| `PATCH` | `/update/{id}` | Partially update an existing product |
| `DELETE` | `/delete/{id}` | Delete a product |

---

### Get All Products

Retrieves a paginated and sorted list of all products. Returns `404` if the table is empty.

```
GET /api/v1/products/getAll
```

**Query parameters** (all optional)

| Parameter | Default | Example |
|---|---|---|
| `page` | `0` | `page=1` |
| `size` | `20` | `size=5` |
| `sort` | `id,asc` | `sort=name,desc` |

**Example request**

```
GET /api/v1/products/getAll?page=0&size=2&sort=name,asc
```

**Responses**

| Status | Description |
|---|---|
| `200 OK` | Products retrieved successfully |
| `404 Not Found` | No products found in the database |

**Example response — 200 OK**

```json
{
  "content": [
    {
      "id": 1,
      "name": "pencil",
      "description": "black pencil",
      "stock": 10,
      "base_price": 200.0,
      "cost_price": 150.0,
      "createdAt": "2024-03-12T18:30:00",
      "updatedAt": "2024-03-12T18:30:00"
    },
    {
      "id": 2,
      "name": "rubber",
      "description": "white rubber",
      "stock": 50,
      "base_price": 300.0,
      "cost_price": 200.0,
      "createdAt": "2024-03-12T18:31:00",
      "updatedAt": "2024-03-12T18:31:00"
    }
  ],
  "currentPage": 0,
  "pageSize": 2,
  "totalElements": 5,
  "totalPages": 3,
  "last": false
}
```

---

### Get Product by ID

```
GET /api/v1/products/get/{id}
```

**Path parameters**

| Parameter | Type | Description |
|---|---|---|
| `id` | `Long` | ID of the product to retrieve |

**Responses**

| Status | Description |
|---|---|
| `200 OK` | Product found |
| `404 Not Found` | No product with the given ID |

**Example response — 200 OK**

```json
{
  "id": 1,
  "name": "pencil",
  "description": "black pencil",
  "stock": 10,
  "base_price": 200.0,
  "cost_price": 150.0,
  "createdAt": "2024-03-12T18:30:00",
  "updatedAt": "2024-03-12T18:30:00"
}
```

---

### Create Product

```
POST /api/v1/products/create
```

**Request body**

```json
{
  "name": "pencil",
  "description": "black pencil",
  "stock": 10,
  "base_price": 200.0,
  "cost_price": 150.0
}
```

**Field constraints**

| Field | Type | Constraints |
|---|---|---|
| `name` | `String` | Required, not blank, must be unique |
| `description` | `String` | Required, not blank |
| `stock` | `Long` | Required, `>= 0` |
| `base_price` | `Double` | Required, `> 0` |
| `cost_price` | `Double` | Required, `> 0` |

**Responses**

| Status | Description |
|---|---|
| `201 Created` | Product created successfully |
| `400 Bad Request` | Validation failed or invalid data |
| `409 Conflict` | A product with that name already exists |

**Example response — 201 Created**

```json
{
  "id": 1,
  "name": "pencil",
  "description": "black pencil",
  "stock": 10,
  "base_price": 200.0,
  "cost_price": 150.0,
  "createdAt": "2024-03-12T18:30:00",
  "updatedAt": "2024-03-12T18:30:00"
}
```

---

### Update Product

Fully replaces all fields of the product with the given ID.

```
PUT /api/v1/products/update/{id}
```

**Path parameters**

| Parameter | Type | Description |
|---|---|---|
| `id` | `Long` | ID of the product to update |

**Request body**

```json
{
  "name": "blue pencil",
  "description": "blue pencil HB",
  "stock": 25,
  "base_price": 220.0,
  "cost_price": 160.0
}
```

Same field constraints as [Create Product](#create-product).

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
  "id": 1,
  "name": "blue pencil",
  "description": "blue pencil HB",
  "stock": 25,
  "base_price": 220.0,
  "cost_price": 160.0,
  "createdAt": "2024-03-12T18:30:00",
  "updatedAt": "2024-03-12T19:15:00"
}
```

---

### Partially Update Product

Updates only the fields included in the request body. Any field that is omitted or sent as `null` is left unchanged.

```
PATCH /api/v1/products/update/{id}
```

**Path parameters**

| Parameter | Type | Description |
|---|---|---|
| `id` | `Long` | ID of the product to patch |

**Request body** (all fields optional)

```json
{
  "stock": 99,
  "base_price": 210.0
}
```

**Field constraints** (applied only when the field is present)

| Field | Type | Constraints |
|---|---|---|
| `name` | `String` | Not blank if provided, must be unique |
| `description` | `String` | Not blank if provided |
| `stock` | `Long` | `>= 0` if provided |
| `base_price` | `Double` | `> 0` if provided |
| `cost_price` | `Double` | `> 0` if provided |

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
  "id": 1,
  "name": "blue pencil",
  "description": "blue pencil HB",
  "stock": 99,
  "base_price": 210.0,
  "cost_price": 160.0,
  "createdAt": "2024-03-12T18:30:00",
  "updatedAt": "2024-03-12T19:20:00"
}
```

---

### Delete Product

```
DELETE /api/v1/products/delete/{id}
```

**Path parameters**

| Parameter | Type | Description |
|---|---|---|
| `id` | `Long` | ID of the product to delete |

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
  "errorMessage": "Product with id 5 not found!"
}
```

| Field | Type | Description |
|---|---|---|
| `statusCode` | `String` | HTTP status name |
| `errorMessage` | `String` | Human-readable error description |

**Possible error messages**

| Status | Message |
|---|---|
| `404 Not Found` | `Product with id {id} not found!` |
| `404 Not Found` | `Products list is empty!` |
| `409 Conflict` | `A product with the name '{name}' already exists!` |
| `400 Bad Request` | `Invalid data entry!` |
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
  ]
}
```

| Field | Type | Description |
|---|---|---|
| `statusCode` | `String` | HTTP status name |
| `errorMessage` | `String` | General error description |
| `fieldErrors` | `String[]` | List of per-field validation messages |

---

## Running Tests

The project has **37 tests** across three test classes, each targeting a different layer.

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
| `ProductPersistenceAdapterTest` | Integration | Uses `@DataJpaTest` with an in-memory H2 database; verifies real persistence operations including audit timestamps (`createdAt`, `updatedAt`) |
