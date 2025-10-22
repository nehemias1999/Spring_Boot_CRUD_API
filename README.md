# Spring-boot-CRUD-API
Spring Boot API to perform CRUD operations on products.

---

## Requirements

- **JDK Version:** 17 (The project is configured to build and run using Java 17)
- **Maven:** 3.9.x or higher  
- **Docker:** 20.x or higher  
- **Docker Compose:** 1.29 or higher  

---

## Running the Entire Stack (API + MySQL) 

You can run both the Spring Boot API and the MySQL database together using Docker Compose.

### Steps

1. Make sure **Docker** and **Docker Compose** are installed on your system.  
2. Verify that the `docker-compose.yaml` file is located in the root directory of the project (next to the `pom.xml` file).  
3. From the project root, run the following command:

   ```bash
   docker-compose up -d
   ```

4. Wait until both containers (the MySQL database and the Spring Boot application) are running.  
   You can check their status with:

   ```bash
   docker ps
   ```

5. Once the containers are up, the application will be available at:

   ```
   http://localhost:8080
   ```

6. To stop and remove the containers, run:

   ```bash
   docker-compose down
   ```

---

## HOW TO USE THE API

### GET ALL

**Request**  
URI: `/products/getAll?[page=pageNumber&size=sizeNumber&sort=fieldName,order]`  
HTTP Verb: `GET`

**Response**
- **200 OK** — Products retrieved successfully  
- **404 NOT FOUND** — No products found  

**Example Response:**
```json
[
  {
    "id":1,
    "name":"pencil",
    "description":"black pencil",
    "stock":10,
    "base_price":200.0,
    "cost_price":150.0
  },
  {
    "id":3,
    "name":"rubber",
    "description":"rubber",
    "stock":50,
    "base_price":300.0,
    "cost_price":200.0
  }
]
```

---

### GET

**Request**  
URI: `/products/get/{id}`  
HTTP Verb: `GET`

**Response**
- **200 OK** — Product found  
- **404 NOT FOUND** — Product not found  

**Example Response:**
```json
{
  "id":1,
  "name":"pencil",
  "description":"black pencil",
  "stock":10,
  "base_price":200.0,
  "cost_price":150.0
}
```

---

### CREATE

**Request**  
URI: `/products/create`  
HTTP Verb: `POST`

**Body:**
```json
{
  "name":"pencil",
  "description":"black pencil",
  "stock":10,
  "base_price":200.0,
  "cost_price":150.0
}
```

**Response**
- **201 CREATED** — Product successfully created  
- **400 BAD REQUEST** — Invalid field values  

**Example Response:**
```json
{
  "id":1,
  "name":"pencil",
  "description":"black pencil",
  "stock":10,
  "base_price":200.0,
  "cost_price":150.0
}
```

---

### UPDATE

**Request**  
URI: `/products/update/{id}`  
HTTP Verb: `PUT`

**Body:**
```json
{
  "name":"new pencil",
  "description":"blue pencil",
  "stock":20,
  "base_price":200.0,
  "cost_price":150.0
}
```

**Response**
- **200 OK** — Product successfully updated  
- **400 BAD REQUEST** — Invalid field values  
- **404 NOT FOUND** — Product not found  

**Example Response:**
```json
{
  "id":1,
  "name":"new pencil",
  "description":"blue pencil",
  "stock":20,
  "base_price":200.0,
  "cost_price":150.0
}
```

---

### DELETE

**Request**  
URI: `/products/delete/{id}`  
HTTP Verb: `DELETE`

**Response**
- **204 NO CONTENT** — Product successfully deleted  
- **404 NOT FOUND** — Product not found  
