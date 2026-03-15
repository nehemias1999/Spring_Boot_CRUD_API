package com.example.productsapi.product.infrastructure.adapter.out.persistence;

import com.example.productsapi.product.domain.Product;
import com.example.productsapi.product.domain.ProductFilter;
import com.example.productsapi.product.infrastructure.config.JpaAuditingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ProductPersistenceAdapter.class, IProductPersistenceMapperImpl.class, JpaAuditingConfig.class})
@DisplayName("ProductPersistenceAdapter")
class ProductPersistenceAdapterTest {

    @Autowired
    private ProductPersistenceAdapter adapter;

    @Autowired
    private TestEntityManager entityManager;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product(null, "pencil", "black pencil", 10L,
                new BigDecimal("200.00"), new BigDecimal("150.00"), null, null);
    }

    // -------------------------------------------------------------------------
    // save + findById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("save and findById")
    class SaveAndFind {

        @Test
        @DisplayName("persists a product and assigns a UUID")
        void whenSaved_assignsUuidAndPersists() {
            Product saved = adapter.save(sampleProduct);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("pencil");
        }

        @Test
        @DisplayName("sets createdAt and leaves updatedAt null on first save")
        void whenSaved_setsCreatedAtAndLeavesUpdatedAtNull() {
            Product saved = adapter.save(sampleProduct);

            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("sets updatedAt when product is updated")
        void whenUpdated_setsUpdatedAt() {
            Product saved = adapter.save(sampleProduct);
            saved.setName("updated pencil");

            Product updated = adapter.save(saved);

            assertThat(updated.getUpdatedAt()).isNotNull();
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
            Optional<Product> found = adapter.findById(java.util.UUID.randomUUID());

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
            adapter.save(new Product(null, "rubber", "white rubber", 20L,
                    new BigDecimal("300.00"), new BigDecimal("200.00"), null, null));

            Page<Product> page = adapter.findAll(PageRequest.of(0, 10), new ProductFilter());

            assertThat(page.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("filters by name when name filter is provided")
        void whenNameFilterProvided_returnsMatchingProducts() {
            adapter.save(sampleProduct);
            adapter.save(new Product(null, "rubber", "white rubber", 20L,
                    new BigDecimal("300.00"), new BigDecimal("200.00"), null, null));

            ProductFilter filter = new ProductFilter("pen", null, null, null);
            Page<Product> page = adapter.findAll(PageRequest.of(0, 10), filter);

            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent().get(0).getName()).isEqualTo("pencil");
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
            assertThat(adapter.existsById(java.util.UUID.randomUUID())).isFalse();
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
            adapter.save(sampleProduct);

            assertThat(adapter.existsByNameAndIdNot("pencil", java.util.UUID.randomUUID())).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // deleteById (soft delete)
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("soft-deletes the product so it can no longer be found")
        void whenDeleted_canNoLongerBeFound() {
            Product saved = adapter.save(sampleProduct);
            java.util.UUID id = saved.getId();

            adapter.deleteById(id);
            entityManager.flush();
            entityManager.clear();

            assertThat(adapter.findById(id)).isEmpty();
            assertThat(adapter.existsById(id)).isFalse();
        }

        @Test
        @DisplayName("soft-deleted product is excluded from findAll results")
        void whenDeleted_excludedFromFindAll() {
            Product saved = adapter.save(sampleProduct);
            adapter.save(new Product(null, "rubber", "white rubber", 20L,
                    new BigDecimal("300.00"), new BigDecimal("200.00"), null, null));

            adapter.deleteById(saved.getId());
            entityManager.flush();
            entityManager.clear();

            Page<Product> page = adapter.findAll(PageRequest.of(0, 10), new ProductFilter());
            assertThat(page.getTotalElements()).isEqualTo(1);
            assertThat(page.getContent().get(0).getName()).isEqualTo("rubber");
        }
    }
}
