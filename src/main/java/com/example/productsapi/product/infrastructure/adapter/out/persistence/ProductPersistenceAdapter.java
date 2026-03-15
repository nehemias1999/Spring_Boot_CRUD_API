package com.example.productsapi.product.infrastructure.adapter.out.persistence;

import com.example.productsapi.product.application.port.out.IProductRepositoryPort;
import com.example.productsapi.product.domain.Product;
import com.example.productsapi.product.domain.ProductFilter;
import com.example.productsapi.product.domain.exception.InvalidDataEntryException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductPersistenceAdapter implements IProductRepositoryPort {

    private final IProductJpaRepository productJpaRepository;
    private final IProductPersistenceMapper mapper;

    @Override
    public Optional<Product> findById(UUID id) {
        log.debug("findById - Querying database for product with id={}", id);
        Optional<Product> result = productJpaRepository.findById(id).map(mapper::toDomain);
        log.debug("findById - Query result for id={}: {}", id, result.isPresent() ? "found" : "not found");
        return result;
    }

    @Override
    public Page<Product> findAll(Pageable pageable, ProductFilter filter) {
        log.debug("findAll - Querying database: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<Product> page = productJpaRepository
                .findAll(ProductSpecification.withFilter(filter), pageable)
                .map(mapper::toDomain);
        log.debug("findAll - Query returned {} records out of {} total",
                page.getNumberOfElements(), page.getTotalElements());
        return page;
    }

    @Override
    public boolean existsById(UUID id) {
        log.debug("existsById - Checking existence in database for product with id={}", id);
        boolean exists = productJpaRepository.existsById(id);
        log.debug("existsById - Product id={} exists: {}", id, exists);
        return exists;
    }

    @Override
    public boolean existsByName(String name) {
        log.debug("existsByName - Checking if name '{}' is already taken", name);
        boolean exists = productJpaRepository.existsByName(name);
        log.debug("existsByName - Name '{}' taken: {}", name, exists);
        return exists;
    }

    @Override
    public boolean existsByNameAndIdNot(String name, UUID id) {
        log.debug("existsByNameAndIdNot - Checking if name '{}' is taken by another product (excluding id={})", name, id);
        boolean exists = productJpaRepository.existsByNameAndIdNot(name, id);
        log.debug("existsByNameAndIdNot - Name '{}' taken by another product: {}", name, exists);
        return exists;
    }

    @Override
    public Product save(Product product) {
        boolean isUpdate = product.getId() != null;
        log.debug("save - {} product in database: name='{}', id={}",
                isUpdate ? "Updating" : "Inserting", product.getName(), product.getId());
        try {
            ProductJpaEntity entity = mapper.toJpaEntity(product);
            if (isUpdate) {
                entity.setUpdatedAt(LocalDateTime.now());
            }
            Product saved = mapper.toDomain(productJpaRepository.save(entity));
            log.debug("save - Product persisted successfully with id={}", saved.getId());
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException | PersistenceException e) {
            log.error("save - Data integrity violation while persisting product name='{}': {}",
                    product.getName(), e.getMessage());
            throw new InvalidDataEntryException();
        }
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("deleteById - Soft-deleting product with id={}", id);
        productJpaRepository.findById(id).ifPresent(entity -> {
            entity.setDeletedAt(LocalDateTime.now());
            productJpaRepository.save(entity);
            log.debug("deleteById - Product with id={} marked as deleted", id);
        });
    }

}
