package com.example.productsapi.product.application.service;

import com.example.productsapi.product.application.port.out.IProductRepositoryPort;
import com.example.productsapi.product.domain.Product;
import com.example.productsapi.product.domain.ProductPatch;
import com.example.productsapi.product.domain.exception.DuplicateProductNameException;
import com.example.productsapi.product.domain.exception.EmptyProductsListException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

    @BeforeEach
    void setUp() {
        sampleProduct = new Product(1L, "pencil", "black pencil", 10L, 200.0, 150.0, null, null);
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
            when(repositoryPort.findAll(any(Pageable.class))).thenReturn(page);

            Page<Product> result = service.getAllProducts(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("pencil");
            verify(repositoryPort).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("throws EmptyProductsListException when no products exist")
        void whenNoProductsExist_throwsEmptyProductsListException() {
            when(repositoryPort.findAll(any(Pageable.class))).thenReturn(Page.empty());

            assertThatThrownBy(() -> service.getAllProducts(pageable))
                    .isInstanceOf(EmptyProductsListException.class)
                    .hasMessage("Products list is empty!");
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
            when(repositoryPort.findById(1L)).thenReturn(Optional.of(sampleProduct));

            Product result = service.getProductById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("pencil");
        }

        @Test
        @DisplayName("throws ProductNotFoundException when product does not exist")
        void whenProductDoesNotExist_throwsProductNotFoundException() {
            when(repositoryPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getProductById(99L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage("Product with id 99 not found!");
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

            assertThat(result.getId()).isEqualTo(1L);
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
            Product incoming = new Product(null, "new pencil", "blue", 20L, 250.0, 180.0, null, null);
            when(repositoryPort.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(repositoryPort.existsByNameAndIdNot("new pencil", 1L)).thenReturn(false);
            when(repositoryPort.save(any(Product.class))).thenReturn(sampleProduct);

            Product result = service.updateProduct(1L, incoming);

            assertThat(result).isNotNull();
            verify(repositoryPort).save(any(Product.class));
        }

        @Test
        @DisplayName("throws ProductNotFoundException when product does not exist")
        void whenProductDoesNotExist_throwsProductNotFoundException() {
            when(repositoryPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateProduct(99L, sampleProduct))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage("Product with id 99 not found!");
        }

        @Test
        @DisplayName("throws DuplicateProductNameException when name is taken by another product")
        void whenNameIsTakenByAnotherProduct_throwsDuplicateProductNameException() {
            Product incoming = new Product(null, "rubber", "eraser", 5L, 100.0, 70.0, null, null);
            when(repositoryPort.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(repositoryPort.existsByNameAndIdNot("rubber", 1L)).thenReturn(true);

            assertThatThrownBy(() -> service.updateProduct(1L, incoming))
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
            when(repositoryPort.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(repositoryPort.existsByNameAndIdNot("updated pencil", 1L)).thenReturn(false);
            when(repositoryPort.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            Product result = service.patchProduct(1L, patch);

            assertThat(result.getName()).isEqualTo("updated pencil");
            assertThat(result.getStock()).isEqualTo(50L);
            assertThat(result.getDescription()).isEqualTo("black pencil"); // unchanged
        }

        @Test
        @DisplayName("throws ProductNotFoundException when product does not exist")
        void whenProductDoesNotExist_throwsProductNotFoundException() {
            when(repositoryPort.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.patchProduct(99L, new ProductPatch()))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("throws DuplicateProductNameException when new name is taken by another product")
        void whenNewNameIsTaken_throwsDuplicateProductNameException() {
            ProductPatch patch = new ProductPatch("rubber", null, null, null, null);
            when(repositoryPort.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(repositoryPort.existsByNameAndIdNot("rubber", 1L)).thenReturn(true);

            assertThatThrownBy(() -> service.patchProduct(1L, patch))
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
            when(repositoryPort.existsById(1L)).thenReturn(true);

            service.deleteProduct(1L);

            verify(repositoryPort).deleteById(1L);
        }

        @Test
        @DisplayName("throws ProductNotFoundException when product does not exist")
        void whenProductDoesNotExist_throwsProductNotFoundException() {
            when(repositoryPort.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteProduct(99L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage("Product with id 99 not found!");

            verify(repositoryPort, never()).deleteById(anyLong());
        }
    }
}
