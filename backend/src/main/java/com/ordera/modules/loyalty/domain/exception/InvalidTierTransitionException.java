package com.ordera.modules.loyalty.domain.exception;

public class InvalidTierTransitionException extends RuntimeException {

    public InvalidTierTransitionException(String message) {
        super(message);
    }
}
