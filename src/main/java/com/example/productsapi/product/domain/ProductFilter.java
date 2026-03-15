package com.example.productsapi.product.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductFilter {

    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long minStock;

}
