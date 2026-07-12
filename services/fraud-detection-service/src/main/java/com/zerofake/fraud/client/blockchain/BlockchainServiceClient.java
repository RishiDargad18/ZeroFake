package com.zerofake.fraud.client.blockchain;

import com.zerofake.fraud.client.blockchain.dto.request.VerifyProductRequest;
import com.zerofake.fraud.client.blockchain.dto.response.ProductHistoryResponse;
import com.zerofake.fraud.client.blockchain.dto.response.VerificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name = "blockchain-service",
        url = "${blockchain.service.url}"
)
public interface BlockchainServiceClient {

    @PostMapping("/api/v1/blockchain/verify-product")
    VerificationResponse verifyProduct(@RequestBody VerifyProductRequest request);

    @GetMapping("/api/v1/blockchain/products/{productId}/history")
    ProductHistoryResponse getProductHistory(@PathVariable UUID productId);

}