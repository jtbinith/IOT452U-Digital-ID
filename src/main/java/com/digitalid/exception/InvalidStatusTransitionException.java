package com.digitalid.exception;

import com.digitalid.domain.IdentityStatus;

public class InvalidStatusTransitionException extends DigitalIdException {

    private final IdentityStatus currentStatus;
    private final IdentityStatus targetStatus;

    public InvalidStatusTransitionException(String identityId, IdentityStatus currentStatus, IdentityStatus targetStatus) {
        super("Cannot transition from " + currentStatus + " to " + targetStatus, identityId);
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public IdentityStatus getCurrentStatus() {
        return currentStatus;
    }
    
    public IdentityStatus getTargetStatus() {
        return targetStatus;
    }
}