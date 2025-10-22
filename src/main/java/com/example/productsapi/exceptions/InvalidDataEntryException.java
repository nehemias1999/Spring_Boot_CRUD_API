package com.example.productsapi.exceptions;

public class InvalidDataEntryException extends RuntimeException {

    public InvalidDataEntryException() {
        super("Invalid data entry!");
    }

}
