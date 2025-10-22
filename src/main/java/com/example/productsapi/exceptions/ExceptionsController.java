package com.example.productsapi.exceptions;

import com.example.productsapi.dto.response.ResponseErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionsController {

    @ExceptionHandler(EmptyProductsListException.class)
    public ResponseEntity<?> emptyProductsList(EmptyProductsListException emptyProductsListException) {
        ResponseErrorDto responseErrorDto = new ResponseErrorDto(HttpStatus.NOT_FOUND, emptyProductsListException.getMessage());
        return new ResponseEntity<>(responseErrorDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<?> productNotFound(ProductNotFoundException productNotFoundException) {
        ResponseErrorDto responseErrorDto = new ResponseErrorDto(HttpStatus.NOT_FOUND, productNotFoundException.getMessage());
        return new ResponseEntity<>(responseErrorDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidDataEntryException.class)
    public ResponseEntity<?> invalidDataEntry(InvalidDataEntryException invalidDataEntryException) {
        ResponseErrorDto responseErrorDto = new ResponseErrorDto(HttpStatus.BAD_REQUEST, invalidDataEntryException.getMessage());
        return new ResponseEntity<>(responseErrorDto, HttpStatus.BAD_REQUEST);
    }

}
