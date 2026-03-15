package com.example.productsapi.product.application.port.out;

import com.example.productsapi.product.domain.Product;
import com.example.productsapi.product.domain.ProductFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface IProductRepositoryPort {

    Optional<Product> findById(UUID id);
    Page<Product> findAll(Pageable pageable, ProductFilter filter);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    Product save(Product product);
    void deleteById(UUID id);

}
