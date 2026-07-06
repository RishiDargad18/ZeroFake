package com.zerofake.auth.dto.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private int status;
    private Boolean success;
    private String message;
    private T data;
}