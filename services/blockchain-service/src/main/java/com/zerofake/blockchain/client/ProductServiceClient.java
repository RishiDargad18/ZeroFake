package com.zerofake.blockchain.client;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.UUID;

/**
 * Notifies the product service that a product's identity is now anchored on the
 * ledger.
 *
 * <p>This replaces the previous arrangement where the browser performed the
 * status update, which put a cross-service workflow in the client and allowed
 * the two systems to disagree if the user closed the tab mid-flow.
 *
 * <p>The caller's bearer token is forwarded, so the product service applies the
 * same authorization it would for a direct call — this service is never granted
 * an ambient privilege of its own.
 *
 * <p>A failure here is logged but never fails the request: the ledger write has
 * already been committed and is the authoritative record. The product's status
 * is a local projection that can be repaired, and undoing a blockchain
 * transaction is not possible.
 */
@Component
public class ProductServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceClient.class);

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private final RestClient restClient;

    public ProductServiceClient(
            @Value("${product.service.url}") String productServiceUrl
    ) {
        // An explicit request factory with bounded timeouts, rather than the
        // auto-configured builder: this call happens immediately after a ledger
        // write, so it must never hang and hold the request open.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);

        this.restClient = RestClient.builder()
                .baseUrl(productServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public void markRegistered(UUID productId) {

        String authorization = currentAuthorizationHeader();

        if (authorization == null) {
            log.warn(
                    "No bearer token available; product {} blockchain status not updated.",
                    productId
            );
            return;
        }

        try {
            restClient.patch()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/products/{id}/blockchain-status")
                            .queryParam("status", "REGISTERED")
                            .build(productId))
                    .header("Authorization", authorization)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Product {} marked REGISTERED in the product service.", productId);

        } catch (Exception ex) {
            log.error(
                    "Product {} was registered on the ledger but the product service "
                            + "could not be updated: {}",
                    productId, ex.getMessage()
            );
        }
    }

    private String currentAuthorizationHeader() {

        if (!(RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes)) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();

        return request.getHeader("Authorization");
    }
}
