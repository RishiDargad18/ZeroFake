package com.zerofake.blockchain.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Standard response envelope used by every ZeroFake REST endpoint.
 */
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ApiResponse<T> {

    private LocalDateTime timestamp;

    private int status;

    private Boolean success;

    private String message;

    private T data;

    public static <T> ApiResponse<T> success(
            HttpStatus status,
            String message,
            T data
    ) {

        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }
}
