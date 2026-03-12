# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

**Build:**
```bash
./mvnw clean package
./mvnw clean package -DskipTests  # skip tests
```

**Run locally (requires MySQL on localhost:3306):**
```bash
./mvnw spring-boot:run
```

**Run with Docker Compose (recommended):**
```bash
docker-compose up -d    # start API + MySQL
docker-compose down     # stop
```

**Tests:**
```bash
./mvnw test                                    # all tests
./mvnw test -Dtest=ClassName                   # single class
./mvnw test -Dtest=ClassName#methodName        # single method
```

## Architecture

Standard Spring Boot layered architecture for a Products CRUD API (`/api/v1/products/`):

- **Controller** (`controllers/ProductsController`) — REST endpoints, handles pagination/sorting params
- **Service** (`services/ProductService`) — business logic, validation, entity↔DTO conversion
- **Repository** (`repositories/IProductRepository`) — extends `JpaRepository<Product, Long>`
- **Entity** (`entities/Product`) — maps to `tbl_products`; uses Lombok
- **DTOs** — `dto/request/ToInsertProductDto` for input, `dto/response/ResponseProductDto` for output
- **Exceptions** — `ExceptionsController` uses `@ControllerAdvice` to handle `ProductNotFoundException`, `EmptyProductsListException`, `InvalidDataEntryException`

## Database

MySQL 8.0 is the target database (`products_db`, table `tbl_products`). The `application.properties` datasource URL points to `mysqldb:3306` (the Docker container hostname). When running outside Docker, you must either:
- Override the datasource URL to `localhost:3306`, or
- Use `docker-compose up -d` so the MySQL container is available

Docker environment variables are configured in `.env` — MySQL is exposed on host port `3307`.

JPA is set to `ddl-auto=update`, so the schema is created/updated automatically on startup.
