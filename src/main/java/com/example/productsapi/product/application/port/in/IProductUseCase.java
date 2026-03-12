package com.example.productsapi.product.application.port.in;

import com.example.productsapi.product.domain.Product;
import com.example.productsapi.product.domain.ProductPatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductUseCase {

    Page<Product> getAllProducts(Pageable pageable);
    Product getProductById(Long id);
    Product createProduct(Product product);
    Product updateProduct(Long id, Product product);
    Product patchProduct(Long id, ProductPatch patch);
    void deleteProduct(Long id);

}
