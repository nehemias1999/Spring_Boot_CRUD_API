package com.example.productsapi.product.infrastructure.adapter.in.web.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ValidationErrorResponse {

    private final HttpStatus statusCode;
    private final String errorMessage;
    private final List<String> fieldErrors;
    private final LocalDateTime timestamp;

    public ValidationErrorResponse(HttpStatus statusCode, String errorMessage, List<String> fieldErrors) {
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.fieldErrors = fieldErrors;
        this.timestamp = LocalDateTime.now();
    }

}
