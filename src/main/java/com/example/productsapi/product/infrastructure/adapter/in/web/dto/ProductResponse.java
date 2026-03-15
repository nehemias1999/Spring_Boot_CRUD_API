package com.example.productsapi.product.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    private UUID id;
    private String name;
    private String description;
    private Long stock;
    private BigDecimal basePrice;
    private BigDecimal costPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
