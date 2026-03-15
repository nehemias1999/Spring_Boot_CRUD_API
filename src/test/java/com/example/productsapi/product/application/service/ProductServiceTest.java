package com.example.productsapi.product.application.service;

import com.example.productsapi.product.application.port.out.IProductRepositoryPort;
import com.example.productsapi.product.domain.Product;
import com.example.productsapi.product.domain.ProductFilter;
import com.example.productsapi.product.domain.ProductPatch;
import com.example.productsapi.product.domain.exception.DuplicateProductNameException;
import com.example.productsapi.product.domain.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock
    private IProductRepositoryPort repositoryPort;

    @InjectMocks
    private ProductService service;

    private Product sampleProduct;
    private Pageable pageable;
    private UUID productId;
    private UUID unknownId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        unknownId = UUID.randomUUID();
        sampleProduct = new Product(productId, "pencil", "black pencil", 10L,
                new BigDecimal("200.00"), new BigDecimal("150.00"), null, null);
        pageable = PageRequest.of(0, 10);
    }

    // -------------------------------------------------------------------------
    // getAllProducts
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("returns a page of products when they exist")
        void whenProductsExist_returnsPage() {
            Page<Product> page = new PageImpl<>(List.of(sampleProduct));
            when(repositoryPort.findAll(any(Pageable.class), any(ProductFilter.class))).thenReturn(page);

            Page<Product> result = service.getAllProducts(pageable, new ProductFilter());

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("pencil");
            verify(repositoryPort).findAll(any(Pageable.class), any(ProductFilter.class));
        }

        @Test
        @DisplayName("returns an empty page when no products exist")
        void whenNoProductsExist_returnsEmptyPage() {
            when(repositoryPort.findAll(any(Pageable.class), any(ProductFilter.class))).thenReturn(Page.empty());

            Page<Product> result = service.getAllProducts(pageable, new ProductFilter());

            assertThat(result.isEmpty()).isTrue();
            verify(repositoryPort).findAll(any(Pageable.class), any(ProductFilter.class));
        }
    }

    // -------------------------------------------------------------------------
    // getProductById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("returns the product when it exists")
        void whenProductExists_returnsProduct() {
            when(repositoryPort.findById(productId)).thenReturn(Optional.of(sampleProduct));

            Product result = service.getProductById(productId);

            assertThat(result.getId()).isEqualTo(productId);
            assertThat(result.getName()).isEqualTo("pencil");
        }

        @Test
        @DisplayName("throws ProductNotFoundException when product does not exist")
        void whenProductDoesNotExist_throwsProductNotFoundException() {
            when(repositoryPort.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getProductById(unknownId))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // createProduct
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("saves and returns the product when name is unique")
        void whenNameIsUnique_savesAndReturnsProduct() {
            when(repositoryPort.existsByName("pencil")).thenReturn(false);
            when(repositoryPort.save(sampleProduct)).thenReturn(sampleProduct);

            Product result = service.createProduct(sampleProduct);

            assertThat(result.getId()).isEqualTo(productId);
            verify(repositoryPort).save(sampleProduct);
        }

        @Test
        @DisplayName("throws DuplicateProductNameException when name already exists")
        void whenNameAlreadyExists_throwsDuplicateProductNameException() {
            when(repositoryPort.existsByName("pencil")).thenReturn(true);

            assertThatThrownBy(() -> service.createProduct(sampleProduct))
                    .isInstanceOf(DuplicateProductNameException.class)
                    .hasMessageContaining("pencil");

            verify(repositoryPort, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // updateProduct
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("updates and returns the product when it exists and name is unique")
        void whenProductExistsAndNameIsUnique_updatesProduct() {
            Product incoming = new Product(null, "new pencil", "blue", 20L,
                    new BigDecimal("250.00"), new BigDecimal("180.00"), null, null);
            when(repositoryPort.findById(productId)).thenReturn(Optional.of(sampleProduct));
            when(repositoryPort.existsByNameAndIdNot("new pencil", productId)).thenReturn(false);
            when(repositoryPort.save(any(Product.class))).thenReturn(sampleProduct);

            Product result = service.updateProduct(productId, incoming);

            assertThat(result).isNotNull();
            verify(repositoryPort).save(any(Product.class));
        }

        @Test
        @DisplayName("throws ProductNotFoundException when product does not exist")
        void whenProductDoesNotExist_throwsProductNotFoundException() {
            when(repositoryPort.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateProduct(unknownId, sampleProduct))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("throws DuplicateProductNameException when name is taken by another product")
        void whenNameIsTakenByAnotherProduct_throwsDuplicateProductNameException() {
            Product incoming = new Product(null, "rubber", "eraser", 5L,
                    new BigDecimal("100.00"), new BigDecimal("70.00"), null, null);
            when(repositoryPort.findById(productId)).thenReturn(Optional.of(sampleProduct));
            when(repositoryPort.existsByNameAndIdNot("rubber", productId)).thenReturn(true);

            assertThatThrownBy(() -> service.updateProduct(productId, incoming))
                    .isInstanceOf(DuplicateProductNameException.class)
                    .hasMessageContaining("rubber");
        }
    }

    // -------------------------------------------------------------------------
    // patchProduct
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("patchProduct")
    class PatchProduct {

        @Test
        @DisplayName("applies only non-null fields and saves")
        void whenPartialFieldsProvided_appliesOnlyThoseFields() {
            ProductPatch patch = new ProductPatch("updated pencil", null, 50L, null, null);
            when(repositoryPort.findById(productId)).thenReturn(Optional.of(sampleProduct));
            when(repositoryPort.existsByNameAndIdNot("updated pencil", productId)).thenReturn(false);
            when(repositoryPort.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            Product result = service.patchProduct(productId, patch);

            assertThat(result.getName()).isEqualTo("updated pencil");
            assertThat(result.getStock()).isEqualTo(50L);
            assertThat(result.getDescription()).isEqualTo("black pencil");
        }

        @Test
        @DisplayName("throws ProductNotFoundException when product does not exist")
        void whenProductDoesNotExist_throwsProductNotFoundException() {
            when(repositoryPort.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.patchProduct(unknownId, new ProductPatch()))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("throws DuplicateProductNameException when new name is taken by another product")
        void whenNewNameIsTaken_throwsDuplicateProductNameException() {
            ProductPatch patch = new ProductPatch("rubber", null, null, null, null);
            when(repositoryPort.findById(productId)).thenReturn(Optional.of(sampleProduct));
            when(repositoryPort.existsByNameAndIdNot("rubber", productId)).thenReturn(true);

            assertThatThrownBy(() -> service.patchProduct(productId, patch))
                    .isInstanceOf(DuplicateProductNameException.class)
                    .hasMessageContaining("rubber");
        }
    }

    // -------------------------------------------------------------------------
    // deleteProduct
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("deletes the product when it exists")
        void whenProductExists_deletesProduct() {
            when(repositoryPort.existsById(productId)).thenReturn(true);

            service.deleteProduct(productId);

            verify(repositoryPort).deleteById(productId);
        }

        @Test
        @DisplayName("throws ProductNotFoundException when product does not exist")
        void whenProductDoesNotExist_throwsProductNotFoundException() {
            when(repositoryPort.existsById(unknownId)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteProduct(unknownId))
                    .isInstanceOf(ProductNotFoundException.class);

            verify(repositoryPort, never()).deleteById(any());
        }
    }
}
