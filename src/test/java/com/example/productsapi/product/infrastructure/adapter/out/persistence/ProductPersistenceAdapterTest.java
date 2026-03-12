package com.example.productsapi.product.infrastructure.adapter.out.persistence;

import com.example.productsapi.product.domain.Product;
import com.example.productsapi.product.infrastructure.config.JpaAuditingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ProductPersistenceAdapter.class, IProductPersistenceMapperImpl.class, JpaAuditingConfig.class})
@DisplayName("ProductPersistenceAdapter")
class ProductPersistenceAdapterTest {

    @Autowired
    private ProductPersistenceAdapter adapter;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product(null, "pencil", "black pencil", 10L, 200.0, 150.0, null, null);
    }

    // -------------------------------------------------------------------------
    // save + findById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("save and findById")
    class SaveAndFind {

        @Test
        @DisplayName("persists a product and assigns an ID")
        void whenSaved_assignsIdAndPersists() {
            Product saved = adapter.save(sampleProduct);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("pencil");
        }

        @Test
        @DisplayName("sets createdAt and updatedAt on first save")
        void whenSaved_setsAuditTimestamps() {
            Product saved = adapter.save(sampleProduct);

            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("findById returns the product after it is saved")
        void whenSaved_canBeFoundById() {
            Product saved = adapter.save(sampleProduct);

            Optional<Product> found = adapter.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("pencil");
        }

        @Test
        @DisplayName("findById returns empty when product does not exist")
        void whenNotFound_returnsEmpty() {
            Optional<Product> found = adapter.findById(999L);

            assertThat(found).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("returns all saved products as a page")
        void whenProductsSaved_returnsPage() {
            adapter.save(sampleProduct);
            adapter.save(new Product(null, "rubber", "white rubber", 20L, 300.0, 200.0, null, null));

            Page<Product> page = adapter.findAll(PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(2);
        }
    }

    // -------------------------------------------------------------------------
    // existsById / existsByName / existsByNameAndIdNot
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("existence checks")
    class ExistenceChecks {

        @Test
        @DisplayName("existsById returns true after saving")
        void whenSaved_existsById() {
            Product saved = adapter.save(sampleProduct);

            assertThat(adapter.existsById(saved.getId())).isTrue();
        }

        @Test
        @DisplayName("existsById returns false for unknown id")
        void whenNotSaved_existsByIdReturnsFalse() {
            assertThat(adapter.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("existsByName returns true when name is taken")
        void whenNameTaken_existsByNameReturnsTrue() {
            adapter.save(sampleProduct);

            assertThat(adapter.existsByName("pencil")).isTrue();
        }

        @Test
        @DisplayName("existsByNameAndIdNot returns false for the same product")
        void whenSameProduct_existsByNameAndIdNotReturnsFalse() {
            Product saved = adapter.save(sampleProduct);

            assertThat(adapter.existsByNameAndIdNot("pencil", saved.getId())).isFalse();
        }

        @Test
        @DisplayName("existsByNameAndIdNot returns true when another product has that name")
        void whenAnotherProductHasSameName_returnsTrue() {
            Product saved = adapter.save(sampleProduct);
            adapter.save(new Product(null, "rubber", "white rubber", 20L, 300.0, 200.0, null, null));

            assertThat(adapter.existsByNameAndIdNot("pencil", 999L)).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // deleteById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("removes the product from the database")
        void whenDeleted_canNoLongerBeFound() {
            Product saved = adapter.save(sampleProduct);
            Long id = saved.getId();

            adapter.deleteById(id);

            assertThat(adapter.findById(id)).isEmpty();
            assertThat(adapter.existsById(id)).isFalse();
        }
    }
}
