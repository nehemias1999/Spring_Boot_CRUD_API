package com.example.productsapi.product.infrastructure.adapter.in.web;

import com.example.productsapi.product.application.port.in.IProductUseCase;
import com.example.productsapi.product.domain.ProductFilter;
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

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final IProductUseCase productUseCase;
    private final IProductWebMapper mapper;

    @Operation(summary = "Get all products", description = "Returns a paginated and sorted list of products. Supports filtering by name (partial match), minPrice, maxPrice, and minStock.")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProductsPagedAndSorted(
            Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long minStock) {
        log.info("GET / - Request received: page={}, size={}, sort={}, name={}, minPrice={}, maxPrice={}, minStock={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(),
                name, minPrice, maxPrice, minStock);

        ProductFilter filter = new ProductFilter(name, minPrice, maxPrice, minStock);
        Page<ProductResponse> page = productUseCase.getAllProducts(pageable, filter)
                .map(mapper::toResponse);

        PagedResponse<ProductResponse> response = new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );

        log.info("GET / - Returning {} products (page {}/{}, total={})",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages(), page.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get product by ID", description = "Returns a single product by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        log.info("GET /{} - Request received", id);

        ProductResponse response = mapper.toResponse(productUseCase.getProductById(id));

        log.info("GET /{} - Product found: name='{}'", id, response.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a product", description = "Creates a new product and returns the created resource.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid data"),
            @ApiResponse(responseCode = "409", description = "A product with that name already exists")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("POST / - Request received: name='{}', stock={}, basePrice={}, costPrice={}",
                request.getName(), request.getStock(), request.getBasePrice(), request.getCostPrice());

        ProductResponse response = mapper.toResponse(productUseCase.createProduct(mapper.toDomain(request)));

        log.info("POST / - Product created successfully: id={}, name='{}'",
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
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("PUT /{} - Request received: name='{}', stock={}, basePrice={}, costPrice={}",
                id, request.getName(), request.getStock(), request.getBasePrice(), request.getCostPrice());

        ProductResponse response = mapper.toResponse(productUseCase.updateProduct(id, mapper.toDomain(request)));

        log.info("PUT /{} - Product updated successfully: name='{}'", id, response.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Partially update a product", description = "Updates only the provided fields of the product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product patched successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "A product with that name already exists")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> patchProduct(
            @PathVariable UUID id,
            @Valid @RequestBody PatchProductRequest request) {
        log.info("PATCH /{} - Request received: {}", id, request);

        ProductResponse response = mapper.toResponse(productUseCase.patchProduct(id, mapper.toPatch(request)));

        log.info("PATCH /{} - Product patched successfully: name='{}'", id, response.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a product", description = "Deletes the product with the given ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        log.info("DELETE /{} - Request received", id);

        productUseCase.deleteProduct(id);

        log.info("DELETE /{} - Product deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

}
