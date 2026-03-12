package com.example.productsapi.product.domain.exception;

public class InvalidDataEntryException extends RuntimeException {

    public InvalidDataEntryException() {
        super("Invalid data entry!");
    }

}
