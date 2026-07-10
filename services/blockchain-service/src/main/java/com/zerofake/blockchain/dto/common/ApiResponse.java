package com.zerofake.blockchain.dto.common;

import lombok.*;

import java.time.LocalDateTime;

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

}