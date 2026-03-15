package com.example.productsapi.product.domain.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(UUID id) {
        super("Product with id " + id + " not found!");
    }

}
