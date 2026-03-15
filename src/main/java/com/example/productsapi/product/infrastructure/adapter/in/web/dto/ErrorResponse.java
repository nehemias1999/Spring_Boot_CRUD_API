package com.example.productsapi.product.infrastructure.adapter.in.web.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {

    private final HttpStatus statusCode;
    private final String errorMessage;
    private final LocalDateTime timestamp;

    public ErrorResponse(HttpStatus statusCode, String errorMessage) {
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now();
    }

}
