package com.zerofake.product.mapper;

import com.zerofake.product.dto.request.CreateProductRequest;
import com.zerofake.product.dto.request.UpdateProductRequest;
import com.zerofake.product.dto.response.ProductResponse;
import com.zerofake.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "batches", ignore = true)
    @Mapping(target = "qrCodePath", ignore = true)
    @Mapping(target = "blockchainStatus", ignore = true)
    @Mapping(target = "active", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "batches", ignore = true)
    @Mapping(target = "qrCodePath", ignore = true)
    @Mapping(target = "blockchainStatus", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(
            UpdateProductRequest request,
            @MappingTarget Product product
    );

    ProductResponse toResponse(Product product);

}