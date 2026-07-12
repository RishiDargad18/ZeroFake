package com.zerofake.fraud.exception;

public class VerificationFailedException extends RuntimeException {

    public VerificationFailedException() {
        super();
    }

    public VerificationFailedException(String message) {
        super(message);
    }

}