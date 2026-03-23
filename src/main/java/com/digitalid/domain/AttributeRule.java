package com.digitalid.domain;

import java.util.List;
import com.digitalid.exception.ImmutableAttributeException;

// rules for which fields can be changed

public class AttributeRule {

    private static final List<IdentityAttribute> IMMUTABLE = List.of(
        IdentityAttribute.ID,
        IdentityAttribute.FIRST_NAME,
        IdentityAttribute.SURNAME,
        IdentityAttribute.GENDER,
        IdentityAttribute.DATE_OF_BIRTH
    );

    public boolean isImmutable(IdentityAttribute attribute) {
        return IMMUTABLE.contains(attribute);
    }

    public void validateMutable(IdentityAttribute attribute) {
        if (isImmutable(attribute)) {
            throw new ImmutableAttributeException(attribute);
        }
    }

    public List<IdentityAttribute> getMutableAttributes() {
        return List.of(IdentityAttribute.NATIONALITY, IdentityAttribute.ADDRESS, IdentityAttribute.POSTCODE);
    }
}