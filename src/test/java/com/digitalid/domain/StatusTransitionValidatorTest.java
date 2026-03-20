package com.digitalid.domain;

import org.junit.jupiter.api.Test;

import com.digitalid.exception.InvalidStatusTransitionException;

import static org.junit.jupiter.api.Assertions.*;

class StatusTransitionValidatorTest {

    private final StatusTransitionValidator validator = new StatusTransitionValidator();

    @Test
    void shouldAllowActiveToSuspended() {
        assertDoesNotThrow(() -> validator.validate("AB-00-01-C", IdentityStatus.ACTIVE, IdentityStatus.SUSPENDED));
    }

    @Test
    void shouldAllowActiveToRevoked() {
        assertDoesNotThrow(() -> validator.validate("AB-00-01-C", IdentityStatus.ACTIVE, IdentityStatus.REVOKED));
    }

    @Test
    void shouldAllowSuspendedToActive() {
        assertDoesNotThrow(() -> validator.validate("AB-00-01-C", IdentityStatus.SUSPENDED, IdentityStatus.ACTIVE));
    }

    @Test
    void shouldAllowSuspendedToRevoked() {
        assertDoesNotThrow(() -> validator.validate("AB-00-01-C", IdentityStatus.SUSPENDED, IdentityStatus.REVOKED));
    }

    @Test
    void shouldRejectRevokedToActive() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> validator.validate("AB-00-01-C", IdentityStatus.REVOKED, IdentityStatus.ACTIVE));
    }

    @Test
    void shouldRejectRevokedToSuspended() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> validator.validate("AB-00-01-C", IdentityStatus.REVOKED, IdentityStatus.SUSPENDED));
    }
}
