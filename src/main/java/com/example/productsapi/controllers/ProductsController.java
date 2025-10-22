package com.example.productsapi.controllers;

import com.example.productsapi.dto.response.ResponseProductDto;
import com.example.productsapi.dto.request.ToInsertProductDto;
import com.example.productsapi.services.IProductService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductsController {

    private final IProductService productService;

    public ProductsController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<ResponseProductDto>> getAllProductsPagedAndSorted(Pageable pageable) {
        List<ResponseProductDto> productsDtoList = productService.getAllProductsPagedAnSorted(pageable);

        return new ResponseEntity<>(productsDtoList, HttpStatus.OK);
    }
    
    @GetMapping("/get/{id}")
    public ResponseEntity<ResponseProductDto> getProductById(@PathVariable Long id) {
        ResponseProductDto responseProductDto = productService.getProductById(id);

        return new ResponseEntity<>(responseProductDto, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseProductDto> createProduct(@RequestBody ToInsertProductDto toInsertProductDto) {
        ResponseProductDto responseProductDto = productService.createProduct(toInsertProductDto);

        return new ResponseEntity<>(responseProductDto, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseProductDto> updateProduct(
            @PathVariable Long id,
            @RequestBody ToInsertProductDto toInsertProductDto) {
        ResponseProductDto responseProductDto = productService.updateProduct(id, toInsertProductDto);

        return new ResponseEntity<>(responseProductDto, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
