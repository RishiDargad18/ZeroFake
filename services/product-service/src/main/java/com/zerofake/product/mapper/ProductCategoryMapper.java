package com.zerofake.product.mapper;

import com.zerofake.product.dto.request.CreateCategoryRequest;
import com.zerofake.product.dto.response.CategoryResponse;
import com.zerofake.product.entity.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {

    @Mapping(target = "active", ignore = true)
    @Mapping(target = "products", ignore = true)
    ProductCategory toEntity(CreateCategoryRequest request);

    CategoryResponse toResponse(ProductCategory productCategory);

}