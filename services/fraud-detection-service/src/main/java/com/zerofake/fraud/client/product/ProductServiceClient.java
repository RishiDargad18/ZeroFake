package com.zerofake.fraud.client.product;

import com.zerofake.fraud.client.common.ApiResponseWrapper;
import com.zerofake.fraud.client.product.dto.BatchResponse;
import com.zerofake.fraud.client.product.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "product-service",
        url = "${product.service.url}",
        configuration = com.zerofake.fraud.config.FeignClientConfig.class
)
public interface ProductServiceClient {

    @GetMapping("/api/v1/products/{productId}")
    ApiResponseWrapper<ProductResponse> getProductById(@PathVariable UUID productId);

    @GetMapping("/api/v1/batches/product/{productId}")
    ApiResponseWrapper<List<BatchResponse>> getBatchesByProduct(@PathVariable UUID productId);
}
