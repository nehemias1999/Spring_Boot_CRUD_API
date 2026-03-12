package com.example.productsapi.product.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be zero or greater")
    private Long stock;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private Double base_price;

    @NotNull(message = "Cost price is required")
    @Positive(message = "Cost price must be positive")
    private Double cost_price;

}
