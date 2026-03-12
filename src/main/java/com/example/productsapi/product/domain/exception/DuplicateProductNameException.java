package com.example.productsapi.product.domain.exception;

public class DuplicateProductNameException extends RuntimeException {

    public DuplicateProductNameException(String name) {
        super("A product with the name '" + name + "' already exists!");
    }

}
