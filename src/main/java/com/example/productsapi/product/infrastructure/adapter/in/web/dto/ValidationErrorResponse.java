package com.example.productsapi.product.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@AllArgsConstructor
public class ValidationErrorResponse {

    private HttpStatus statusCode;
    private String errorMessage;
    private List<String> fieldErrors;

}
