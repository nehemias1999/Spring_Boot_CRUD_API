package com.example.productsapi.product.application.port.out;

import com.example.productsapi.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IProductRepositoryPort {

    Optional<Product> findById(Long id);
    Page<Product> findAll(Pageable pageable);
    boolean existsById(Long id);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    Product save(Product product);
    void deleteById(Long id);

}
