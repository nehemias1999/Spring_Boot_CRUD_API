package com.example.productsapi.services;

import com.example.productsapi.dto.response.ResponseProductDto;
import com.example.productsapi.dto.request.ToInsertProductDto;
import com.example.productsapi.entities.Product;
import com.example.productsapi.exceptions.EmptyProductsListException;
import com.example.productsapi.exceptions.InvalidDataEntryException;
import com.example.productsapi.exceptions.ProductNotFoundException;
import com.example.productsapi.repositories.IProductRepository;
import jakarta.persistence.PersistenceException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService implements IProductService {

    private final IProductRepository productRepository;

    public ProductService(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Contract("_ -> new")
    private @NotNull Product getProduct(@NotNull ToInsertProductDto toInsertProductDto) {
        return new Product(
                null,
                toInsertProductDto.getName(),
                toInsertProductDto.getDescription(),
                toInsertProductDto.getStock(),
                toInsertProductDto.getBase_price(),
                toInsertProductDto.getCost_price()
        );
    }

    @Contract("_ -> new")
    private @NotNull ResponseProductDto getProductDto(@NotNull Product product) {
        return new ResponseProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getStock(),
                product.getBase_price(),
                product.getCost_price()
        );
    }

    @Override
    public List<ResponseProductDto> getAllProductsPagedAnSorted(@NotNull Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "id"))
                )
        );

        if(productsPage.isEmpty())
            throw new EmptyProductsListException();

        return productsPage.map(
                this::getProductDto
        ).toList();
    }

    @Override
    public ResponseProductDto getProductById(@NotNull Long id) {
        Optional<Product> productOptional = productRepository.findById(id);

        if(productOptional.isEmpty())
            throw new ProductNotFoundException();

        return this.getProductDto(productOptional.get());
    }

    @Override
    public ResponseProductDto createProduct(@NotNull ToInsertProductDto toInsertProductDto) {
        Product product = this.getProduct(toInsertProductDto);

        try {
            return this.getProductDto(productRepository.save(product));
        } catch (DataIntegrityViolationException | JpaSystemException | PersistenceException e) {
            throw new InvalidDataEntryException();
        }
    }

    @Override
    public ResponseProductDto updateProduct(Long id, ToInsertProductDto toInsertProductDto) {
        if(!productRepository.existsById(id))
            throw new ProductNotFoundException();

        Product product = this.getProduct(toInsertProductDto);

        product.setId(id);

        try {
            return this.getProductDto(productRepository.save(product));
        } catch (DataIntegrityViolationException | JpaSystemException | PersistenceException e) {
            throw new InvalidDataEntryException();
        }
    }

    @Override
    public void deleteProduct(Long id) {
        if(!productRepository.existsById(id))
            throw new ProductNotFoundException();

        productRepository.deleteById(id);
    }

}
