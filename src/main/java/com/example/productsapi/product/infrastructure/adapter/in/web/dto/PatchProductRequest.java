package com.example.productsapi.product.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatchProductRequest {

    @Size(min = 1, message = "Name must not be blank if provided")
    private String name;

    @Size(min = 1, message = "Description must not be blank if provided")
    private String description;

    @Min(value = 0, message = "Stock must be zero or greater")
    private Long stock;

    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;

    @Positive(message = "Cost price must be positive")
    private BigDecimal costPrice;

}
