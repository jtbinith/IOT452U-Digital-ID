package com.digitalid.domain;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.digitalid.exception.InvalidStatusTransitionException;

import static org.junit.jupiter.api.Assertions.*;

class DigitalIDTest {

    private DigitalID identity;

    @BeforeEach
    void setUp() {
        identity = new DigitalID("AB-00-01-C", "Jane", "Smith", "Female",
                LocalDate.of(1995, 6, 15), "British", "10 High Street", "E1 7AA");
    }

    @Test
    void newIdentityShouldBeActive() {
        assertTrue(identity.isActive());
        assertFalse(identity.isSuspended());
        assertFalse(identity.isRevoked());
    }

    @Test
    void shouldReturnFullName() {
        assertEquals("Jane Smith", identity.getFullName());
    }

    @Test
    void suspendShouldChangeStatusToSuspended() {
        identity.suspend();
        assertTrue(identity.isSuspended());
        assertFalse(identity.isActive());
    }

    @Test
    void activateShouldReactivateSuspendedIdentity() {
        identity.suspend();
        identity.activate();
        assertTrue(identity.isActive());
    }

    @Test
    void revokeShouldChangeStatusToRevoked() {
        identity.revoke();
        assertTrue(identity.isRevoked());
    }

    @Test
    void shouldNotSuspendRevokedIdentity() {
        identity.revoke();
        assertThrows(InvalidStatusTransitionException.class, () -> identity.suspend());
    }

    @Test
    void shouldNotActivateRevokedIdentity() {
        identity.revoke();
        assertThrows(InvalidStatusTransitionException.class, () -> identity.activate());
    }

    @Test
    void shouldNotUpdateNationalityWhenRevoked() {
        identity.revoke();
        assertThrows(InvalidStatusTransitionException.class, () -> identity.updateNationality("French"));
    }

    @Test
    void shouldUpdateNationalityWhenActive() {
        identity.updateNationality("French");
        assertEquals("French", identity.getNationality());
    }

    @Test
    void shouldUpdateAddressWhenActive() {
        identity.updateAddress("20 New Road");
        assertEquals("20 New Road", identity.getAddress());
    }

    @Test
    void shouldSetAndRemoveRestriction() {
        identity.setRestriction(true);
        assertTrue(identity.isRestricted());
        identity.setRestriction(false);
        assertFalse(identity.isRestricted());
    }
}
