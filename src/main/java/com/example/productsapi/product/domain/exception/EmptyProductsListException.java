package com.example.productsapi.product.domain.exception;

public class EmptyProductsListException extends RuntimeException {

    public EmptyProductsListException() {
        super("Products list is empty!");
    }

}
