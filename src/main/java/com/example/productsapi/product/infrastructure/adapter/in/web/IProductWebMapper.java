package com.example.productsapi.product.infrastructure.adapter.in.web;

import com.example.productsapi.product.domain.Product;
import com.example.productsapi.product.domain.ProductPatch;
import com.example.productsapi.product.infrastructure.adapter.in.web.dto.CreateProductRequest;
import com.example.productsapi.product.infrastructure.adapter.in.web.dto.PatchProductRequest;
import com.example.productsapi.product.infrastructure.adapter.in.web.dto.ProductResponse;
import com.example.productsapi.product.infrastructure.adapter.in.web.dto.UpdateProductRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IProductWebMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toDomain(CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toDomain(UpdateProductRequest request);

    ProductPatch toPatch(PatchProductRequest request);

    ProductResponse toResponse(Product product);

}
