package com.digitalid.exception;

import com.digitalid.domain.IdentityAttribute;

public class ImmutableAttributeException extends DigitalIdException {

    private final IdentityAttribute attribute;

    public ImmutableAttributeException(IdentityAttribute attribute) {
        super(attribute + " is immutable and cannot be changed", null);
        this.attribute = attribute;
    }

    public IdentityAttribute getAttribute() {
        return attribute;
    }
}

