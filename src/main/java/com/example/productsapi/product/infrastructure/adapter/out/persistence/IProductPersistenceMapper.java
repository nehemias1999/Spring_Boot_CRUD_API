package com.example.productsapi.product.infrastructure.adapter.out.persistence;

import com.example.productsapi.product.domain.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IProductPersistenceMapper {

    Product toDomain(ProductJpaEntity entity);

    ProductJpaEntity toJpaEntity(Product product);

}
