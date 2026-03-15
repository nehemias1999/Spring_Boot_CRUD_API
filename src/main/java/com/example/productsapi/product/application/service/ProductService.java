package com.example.productsapi.product.application.service;

import com.example.productsapi.product.application.port.in.IProductUseCase;
import com.example.productsapi.product.application.port.out.IProductRepositoryPort;
import com.example.productsapi.product.domain.Product;
import com.example.productsapi.product.domain.ProductFilter;
import com.example.productsapi.product.domain.ProductPatch;
import com.example.productsapi.product.domain.exception.DuplicateProductNameException;
import com.example.productsapi.product.domain.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService implements IProductUseCase {

    private final IProductRepositoryPort productRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable, ProductFilter filter) {
        log.debug("getAllProducts - Fetching page {} with size {} and sort '{}'",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<Product> productsPage = productRepositoryPort.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "id"))
                ),
                filter
        );

        log.debug("getAllProducts - Found {} products (total elements: {})",
                productsPage.getNumberOfElements(), productsPage.getTotalElements());
        return productsPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(UUID id) {
        log.debug("getProductById - Looking for product with id={}", id);

        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> {
                    log.warn("getProductById - Product with id={} not found", id);
                    return new ProductNotFoundException(id);
                });

        log.debug("getProductById - Product found: id={}, name='{}'", product.getId(), product.getName());
        return product;
    }

    @Override
    @Transactional
    public Product createProduct(Product product) {
        log.debug("createProduct - Checking name uniqueness for '{}'", product.getName());

        if (productRepositoryPort.existsByName(product.getName())) {
            log.warn("createProduct - Product with name '{}' already exists", product.getName());
            throw new DuplicateProductNameException(product.getName());
        }

        log.debug("createProduct - Persisting new product: name='{}', stock={}", product.getName(), product.getStock());
        Product saved = productRepositoryPort.save(product);

        log.info("createProduct - Product persisted with id={}, name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public Product updateProduct(UUID id, Product product) {
        log.debug("updateProduct - Looking for product with id={}", id);

        Product existing = productRepositoryPort.findById(id)
                .orElseThrow(() -> {
                    log.warn("updateProduct - Product with id={} not found, cannot update", id);
                    return new ProductNotFoundException(id);
                });

        if (productRepositoryPort.existsByNameAndIdNot(product.getName(), id)) {
            log.warn("updateProduct - Name '{}' is already taken by another product", product.getName());
            throw new DuplicateProductNameException(product.getName());
        }

        if (product.getName() != null)        existing.setName(product.getName());
        if (product.getDescription() != null) existing.setDescription(product.getDescription());
        if (product.getStock() != null)       existing.setStock(product.getStock());
        if (product.getBasePrice() != null)   existing.setBasePrice(product.getBasePrice());
        if (product.getCostPrice() != null)   existing.setCostPrice(product.getCostPrice());

        log.debug("updateProduct - Persisting updated data for product id={}: name='{}', stock={}",
                id, existing.getName(), existing.getStock());
        Product updated = productRepositoryPort.save(existing);

        log.info("updateProduct - Product id={} updated successfully: name='{}'", id, updated.getName());
        return updated;
    }

    @Override
    @Transactional
    public Product patchProduct(UUID id, ProductPatch patch) {
        log.debug("patchProduct - Looking for product with id={}", id);

        Product existing = productRepositoryPort.findById(id)
                .orElseThrow(() -> {
                    log.warn("patchProduct - Product with id={} not found, cannot patch", id);
                    return new ProductNotFoundException(id);
                });

        if (patch.getName() != null && productRepositoryPort.existsByNameAndIdNot(patch.getName(), id)) {
            log.warn("patchProduct - Name '{}' is already taken by another product", patch.getName());
            throw new DuplicateProductNameException(patch.getName());
        }

        if (patch.getName() != null)        existing.setName(patch.getName());
        if (patch.getDescription() != null) existing.setDescription(patch.getDescription());
        if (patch.getStock() != null)       existing.setStock(patch.getStock());
        if (patch.getBasePrice() != null)   existing.setBasePrice(patch.getBasePrice());
        if (patch.getCostPrice() != null)   existing.setCostPrice(patch.getCostPrice());

        log.debug("patchProduct - Persisting patched product id={}", id);
        Product patched = productRepositoryPort.save(existing);

        log.info("patchProduct - Product id={} patched successfully: name='{}'", id, patched.getName());
        return patched;
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        log.debug("deleteProduct - Checking existence of product with id={}", id);

        if (!productRepositoryPort.existsById(id)) {
            log.warn("deleteProduct - Product with id={} not found, cannot delete", id);
            throw new ProductNotFoundException(id);
        }

        productRepositoryPort.deleteById(id);
        log.info("deleteProduct - Product with id={} deleted successfully", id);
    }

}
