package com.zerofake.fraud.client.blockchain;

import com.zerofake.fraud.client.blockchain.dto.request.VerifyProductRequest;
import com.zerofake.fraud.client.blockchain.dto.response.ProductHistoryResponse;
import com.zerofake.fraud.client.blockchain.dto.response.VerificationResponse;
import com.zerofake.fraud.client.common.ApiResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

/**
 * Read-only access to the ledger, through the blockchain service.
 *
 * <p>This service never writes to the ledger. Verification is an
 * investigation, not a custody event, so no ownership transfer endpoint is
 * exposed here.
 */
@FeignClient(
        name = "blockchain-service",
        url = "${blockchain.service.url}",
        configuration = com.zerofake.fraud.config.FeignClientConfig.class
)
public interface BlockchainServiceClient {

    @PostMapping("/api/v1/blockchain/verify-product")
    ApiResponseWrapper<VerificationResponse> verifyProduct(@RequestBody VerifyProductRequest request);

    @GetMapping("/api/v1/blockchain/products/{productId}/history")
    ApiResponseWrapper<ProductHistoryResponse> getProductHistory(@PathVariable UUID productId);
}
