package com.zerofake.fraud.exception;

public class FraudDetectedException extends RuntimeException {

    public FraudDetectedException() {
        super();
    }

    public FraudDetectedException(String message) {
        super(message);
    }

}