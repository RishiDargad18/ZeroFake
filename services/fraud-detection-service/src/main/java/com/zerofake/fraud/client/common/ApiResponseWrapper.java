package com.zerofake.fraud.client.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Mirrors the response envelope returned by every other ZeroFake service.
 *
 * <p>Declared explicitly so that Feign deserialises the envelope rather than
 * silently binding it onto the payload type and producing an object with every
 * field null.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseWrapper<T> {

    private LocalDateTime timestamp;

    private int status;

    private Boolean success;

    private String message;

    private T data;
}
