package com.zerofake.fraud.exception;

public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException() {
        super();
    }

    public ExternalServiceException(String message) {
        super(message);
    }

}