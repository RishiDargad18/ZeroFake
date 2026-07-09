package com.zerofake.product.mapper;

import com.zerofake.product.dto.request.CreateBatchRequest;
import com.zerofake.product.dto.request.UpdateBatchRequest;
import com.zerofake.product.dto.response.BatchResponse;
import com.zerofake.product.entity.ProductBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductBatchMapper {
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "status", ignore = true)
    ProductBatch toEntity(CreateBatchRequest request);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(
            UpdateBatchRequest request,
            @MappingTarget ProductBatch productBatch
    );

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productCode", target = "productCode")
    BatchResponse toResponse(ProductBatch productBatch);

}