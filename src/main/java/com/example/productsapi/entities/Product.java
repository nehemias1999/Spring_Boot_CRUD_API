package com.example.productsapi.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Product {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Id Long id;
    private String name;
    private String description;
    private Long stock;
    private Double base_price;
    private Double cost_price;

}
