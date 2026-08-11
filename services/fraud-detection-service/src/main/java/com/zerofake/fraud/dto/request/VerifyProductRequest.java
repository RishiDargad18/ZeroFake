package com.zerofake.fraud.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A request to verify a scanned product.
 *
 * <p>The scanner's identity is taken from their access token and is deliberately
 * absent here. Accepting a user id and role in the body would let any caller
 * attribute a scan to someone else and defeat every rule built on scan history.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyProductRequest {

    @NotNull(message = "Product ID is required.")
    private UUID productId;

    @Size(max = 45, message = "IP address must not exceed 45 characters.")
    private String ipAddress;

    @Size(max = 255, message = "Device information must not exceed 255 characters.")
    private String deviceInfo;

    @Size(max = 255, message = "Location must not exceed 255 characters.")
    private String location;
}
