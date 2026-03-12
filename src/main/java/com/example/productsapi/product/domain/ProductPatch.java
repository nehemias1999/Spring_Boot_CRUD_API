package com.example.productsapi.product.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPatch {

    private String name;
    private String description;
    private Long stock;
    private Double base_price;
    private Double cost_price;

}
