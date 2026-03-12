package com.example.productsapi.product.infrastructure.adapter.in.web;

import com.example.productsapi.product.application.port.in.IProductUseCase;
import com.example.productsapi.product.domain.Product;
import com.example.productsapi.product.domain.ProductPatch;
import com.example.productsapi.product.domain.exception.DuplicateProductNameException;
import com.example.productsapi.product.domain.exception.ProductNotFoundException;
import com.example.productsapi.product.infrastructure.adapter.in.web.dto.CreateProductRequest;
import com.example.productsapi.product.infrastructure.adapter.in.web.dto.ProductResponse;
import com.example.productsapi.product.infrastructure.adapter.in.web.dto.UpdateProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IProductUseCase productUseCase;

    @MockBean
    private IProductWebMapper mapper;

    private Product sampleProduct;
    private ProductResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product(1L, "pencil", "black pencil", 10L, 200.0, 150.0,
                LocalDateTime.now(), LocalDateTime.now());
        sampleResponse = new ProductResponse(1L, "pencil", "black pencil", 10L, 200.0, 150.0,
                sampleProduct.getCreatedAt(), sampleProduct.getUpdatedAt());
    }

    // -------------------------------------------------------------------------
    // GET /getAll
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /getAll")
    class GetAll {

        @Test
        @DisplayName("returns 200 with paginated response when products exist")
        void whenProductsExist_returns200WithPagedResponse() throws Exception {
            Page<Product> page = new PageImpl<>(List.of(sampleProduct));
            when(productUseCase.getAllProducts(any(Pageable.class))).thenReturn(page);
            when(mapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/products/getAll"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value("pencil"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }
    }

    // -------------------------------------------------------------------------
    // GET /get/{id}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /get/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 when product is found")
        void whenProductExists_returns200() throws Exception {
            when(productUseCase.getProductById(1L)).thenReturn(sampleProduct);
            when(mapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/products/get/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("pencil"));
        }

        @Test
        @DisplayName("returns 404 when product is not found")
        void whenProductDoesNotExist_returns404() throws Exception {
            when(productUseCase.getProductById(99L)).thenThrow(new ProductNotFoundException(99L));

            mockMvc.perform(get("/api/v1/products/get/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorMessage").value("Product with id 99 not found!"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /create
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /create")
    class Create {

        @Test
        @DisplayName("returns 201 when product is created successfully")
        void whenValidRequest_returns201() throws Exception {
            CreateProductRequest request = new CreateProductRequest("pencil", "black pencil", 10L, 200.0, 150.0);
            when(mapper.toDomain(any(CreateProductRequest.class))).thenReturn(sampleProduct);
            when(productUseCase.createProduct(sampleProduct)).thenReturn(sampleProduct);
            when(mapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/products/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("pencil"));
        }

        @Test
        @DisplayName("returns 400 when request body fails validation")
        void whenInvalidRequest_returns400WithFieldErrors() throws Exception {
            CreateProductRequest invalidRequest = new CreateProductRequest("", null, -5L, -10.0, 0.0);

            mockMvc.perform(post("/api/v1/products/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors").isArray());
        }

        @Test
        @DisplayName("returns 409 when product name already exists")
        void whenDuplicateName_returns409() throws Exception {
            CreateProductRequest request = new CreateProductRequest("pencil", "black pencil", 10L, 200.0, 150.0);
            when(mapper.toDomain(any(CreateProductRequest.class))).thenReturn(sampleProduct);
            when(productUseCase.createProduct(sampleProduct)).thenThrow(new DuplicateProductNameException("pencil"));

            mockMvc.perform(post("/api/v1/products/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorMessage").value("A product with the name 'pencil' already exists!"));
        }
    }

    // -------------------------------------------------------------------------
    // PUT /update/{id}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PUT /update/{id}")
    class Update {

        @Test
        @DisplayName("returns 200 when product is updated successfully")
        void whenValidRequest_returns200() throws Exception {
            UpdateProductRequest request = new UpdateProductRequest("pencil v2", "blue pencil", 20L, 250.0, 180.0);
            when(mapper.toDomain(any(UpdateProductRequest.class))).thenReturn(sampleProduct);
            when(productUseCase.updateProduct(anyLong(), any(Product.class))).thenReturn(sampleProduct);
            when(mapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

            mockMvc.perform(put("/api/v1/products/update/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("returns 404 when product does not exist")
        void whenProductDoesNotExist_returns404() throws Exception {
            UpdateProductRequest request = new UpdateProductRequest("pencil", "desc", 10L, 200.0, 150.0);
            when(mapper.toDomain(any(UpdateProductRequest.class))).thenReturn(sampleProduct);
            when(productUseCase.updateProduct(anyLong(), any(Product.class)))
                    .thenThrow(new ProductNotFoundException(99L));

            mockMvc.perform(put("/api/v1/products/update/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // PATCH /update/{id}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PATCH /update/{id}")
    class PatchProduct {

        @Test
        @DisplayName("returns 200 when product is patched successfully")
        void whenValidRequest_returns200() throws Exception {
            String patchBody = "{\"stock\": 99}";
            when(mapper.toPatch(any())).thenReturn(new ProductPatch(null, null, 99L, null, null));
            when(productUseCase.patchProduct(anyLong(), any(ProductPatch.class))).thenReturn(sampleProduct);
            when(mapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

            mockMvc.perform(patch("/api/v1/products/update/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(patchBody))
                    .andExpect(status().isOk());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /delete/{id}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("DELETE /delete/{id}")
    class Delete {

        @Test
        @DisplayName("returns 204 when product is deleted successfully")
        void whenProductExists_returns204() throws Exception {
            mockMvc.perform(delete("/api/v1/products/delete/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when product does not exist")
        void whenProductDoesNotExist_returns404() throws Exception {
            doThrow(new ProductNotFoundException(99L)).when(productUseCase).deleteProduct(99L);

            mockMvc.perform(delete("/api/v1/products/delete/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorMessage").value("Product with id 99 not found!"));
        }
    }
}
