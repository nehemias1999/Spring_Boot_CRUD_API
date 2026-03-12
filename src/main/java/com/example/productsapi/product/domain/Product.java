package com.example.productsapi.product.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Product {

    private Long id;
    private String name;
    private String description;
    private Long stock;
    private Double base_price;
    private Double cost_price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
