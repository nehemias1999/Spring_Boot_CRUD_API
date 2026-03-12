package com.example.productsapi.product.infrastructure.adapter.in.web;

import com.example.productsapi.product.application.port.in.IProductUseCase;
import com.example.productsapi.product.infrastructure.adapter.in.web.dto.CreateProductRequest;
import com.example.productsapi.product.infrastructure.adapter.in.web.dto.PagedResponse;
import com.example.productsapi.product.infrastructure.adapter.in.web.dto.PatchProductRequest;
import com.example.productsapi.product.infrastructure.adapter.in.web.dto.ProductResponse;
import com.example.productsapi.product.infrastructure.adapter.in.web.dto.UpdateProductRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final IProductUseCase productUseCase;
    private final IProductWebMapper mapper;

    @Operation(summary = "Get all products", description = "Returns a paginated and sorted list of all products.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No products found")
    })
    @GetMapping("/getAll")
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProductsPagedAndSorted(Pageable pageable) {
        log.info("GET /getAll - Request received: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<ProductResponse> page = productUseCase.getAllProducts(pageable)
                .map(mapper::toResponse);

        PagedResponse<ProductResponse> response = new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );

        log.info("GET /getAll - Returning {} products (page {}/{})",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get product by ID", description = "Returns a single product by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/get/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.info("GET /get/{} - Request received", id);

        ProductResponse response = mapper.toResponse(productUseCase.getProductById(id));

        log.info("GET /get/{} - Product found: name='{}'", id, response.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a product", description = "Creates a new product and returns the created resource.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid data"),
            @ApiResponse(responseCode = "409", description = "A product with that name already exists")
    })
    @PostMapping("/create")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("POST /create - Request received: name='{}', stock={}, basePrice={}, costPrice={}",
                request.getName(), request.getStock(), request.getBase_price(), request.getCost_price());

        ProductResponse response = mapper.toResponse(productUseCase.createProduct(mapper.toDomain(request)));

        log.info("POST /create - Product created successfully: id={}, name='{}'",
                response.getId(), response.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update a product", description = "Fully replaces the product with the given ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "A product with that name already exists")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("PUT /update/{} - Request received: name='{}', stock={}, basePrice={}, costPrice={}",
                id, request.getName(), request.getStock(), request.getBase_price(), request.getCost_price());

        ProductResponse response = mapper.toResponse(productUseCase.updateProduct(id, mapper.toDomain(request)));

        log.info("PUT /update/{} - Product updated successfully: name='{}'", id, response.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Partially update a product", description = "Updates only the provided fields of the product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product patched successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "A product with that name already exists")
    })
    @PatchMapping("/update/{id}")
    public ResponseEntity<ProductResponse> patchProduct(
            @PathVariable Long id,
            @Valid @RequestBody PatchProductRequest request) {
        log.info("PATCH /update/{} - Request received: {}", id, request);

        ProductResponse response = mapper.toResponse(productUseCase.patchProduct(id, mapper.toPatch(request)));

        log.info("PATCH /update/{} - Product patched successfully: name='{}'", id, response.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a product", description = "Deletes the product with the given ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /delete/{} - Request received", id);

        productUseCase.deleteProduct(id);

        log.info("DELETE /delete/{} - Product deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

}
