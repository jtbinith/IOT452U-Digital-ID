package com.digitalid.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.digitalid.exception.InvalidStatusTransitionException;

// state machine rules

public class StatusTransitionValidator {

    private final Map<IdentityStatus, List<IdentityStatus>> allowedTransitions;

    public StatusTransitionValidator() {
        allowedTransitions = new HashMap<>();
        allowedTransitions.put(IdentityStatus.ACTIVE, List.of(IdentityStatus.SUSPENDED, IdentityStatus.REVOKED));
        allowedTransitions.put(IdentityStatus.SUSPENDED, List.of(IdentityStatus.ACTIVE, IdentityStatus.REVOKED));
        allowedTransitions.put(IdentityStatus.REVOKED, List.of());
    }

    public void validate(String identityId, IdentityStatus current, IdentityStatus target) {
        List<IdentityStatus> allowed = allowedTransitions.get(current);
        if (!allowed.contains(target)) {
            throw new InvalidStatusTransitionException(identityId, current, target);
        }
    }

    public List<IdentityStatus> getAllowedTransitions(IdentityStatus current) {
        return allowedTransitions.get(current);
    }
}
