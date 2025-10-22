package com.example.productsapi.services;

import com.example.productsapi.dto.response.ResponseProductDto;
import com.example.productsapi.dto.request.ToInsertProductDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService {

    List<ResponseProductDto> getAllProductsPagedAnSorted(Pageable pageable);
    ResponseProductDto getProductById(Long id);
    ResponseProductDto createProduct(ToInsertProductDto toInsertproductDto);
    ResponseProductDto updateProduct(Long id, ToInsertProductDto toInsertProductDto);
    void deleteProduct(Long id);

}
