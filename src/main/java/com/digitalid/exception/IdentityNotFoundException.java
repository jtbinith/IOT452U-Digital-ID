package com.digitalid.exception;

public class IdentityNotFoundException extends DigitalIdException {

    public IdentityNotFoundException(String identityId) {
        super("Identity " + identityId + " not found", identityId);
    }
}