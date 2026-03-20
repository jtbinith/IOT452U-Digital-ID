package com.digitalid.exception;

// abstract base class

public abstract class DigitalIdException extends RuntimeException {
    protected final String identityId;

    protected DigitalIdException(String message, String identityId) {
        super(message);
        this.identityId = identityId;
    }

    public String getIdentityId() {
        return identityId;
    }
}
