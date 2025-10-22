package com.example.productsapi.exceptions;

public class EmptyProductsListException extends RuntimeException {

    public EmptyProductsListException() {
        super("Products list is empty!");
    }

}
