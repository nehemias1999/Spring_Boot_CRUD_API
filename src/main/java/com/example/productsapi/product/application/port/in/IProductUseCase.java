package com.example.productsapi.product.application.port.in;

import com.example.productsapi.product.domain.Product;
import com.example.productsapi.product.domain.ProductFilter;
import com.example.productsapi.product.domain.ProductPatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IProductUseCase {

    Page<Product> getAllProducts(Pageable pageable, ProductFilter filter);
    Product getProductById(UUID id);
    Product createProduct(Product product);
    Product updateProduct(UUID id, Product product);
    Product patchProduct(UUID id, ProductPatch patch);
    void deleteProduct(UUID id);

}
