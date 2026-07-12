package com.zerofake.fraud.client.product;

import com.zerofake.fraud.client.product.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "product-service",
        url = "${product.service.url}"
)
public interface ProductServiceClient {

    @GetMapping("/api/v1/products/{productId}")
    ProductResponse getProductById(@PathVariable UUID productId);

}