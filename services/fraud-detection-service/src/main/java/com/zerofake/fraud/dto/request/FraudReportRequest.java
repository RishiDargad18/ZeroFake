package com.zerofake.fraud.dto.request;

import com.zerofake.fraud.constant.FraudType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A fraud report raised manually by a user.
 *
 * <p>The reporter's identity comes from their access token, not from this body.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudReportRequest {

    @NotNull(message = "Product ID is required.")
    private UUID productId;

    /**
     * What the reporter believes is wrong. Optional: a member of the public
     * reporting a suspicious item cannot be expected to classify it, so this
     * defaults to {@link FraudType#SUSPICIOUS_ACTIVITY}.
     */
    private FraudType fraudType;

    @NotBlank(message = "A description of the suspected fraud is required.")
    @Size(max = 1000, message = "Description must not exceed 1000 characters.")
    private String description;
}
