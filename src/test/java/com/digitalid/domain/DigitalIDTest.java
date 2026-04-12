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
        assertThrows(IllegalStateException.class, () -> identity.updateNationality("French"));
    }

    @Test
    void shouldNotUpdateAddressWhenRevoked() {
        identity.revoke();
        assertThrows(IllegalStateException.class, () -> identity.updateAddress("86 New Road"));
    }

    @Test
    void shouldNotSetRestrictionWhenRevoked() {
        identity.revoke();
        assertThrows(IllegalStateException.class, () -> identity.setRestriction(true));
    }

    @Test
    void newIdentityShouldNotBeRestricted() {
        assertFalse(identity.isRestricted());
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

    @Test
    void shouldUpdatePostcodeWhenActive() {
        identity.updatePostcode("CB1 8GB");
        assertEquals("CB1 8GB", identity.getPostcode());
    }

    @Test
    void shouldNotUpdatePostcodeWhenRevoked() {
        identity.revoke();
        assertThrows(IllegalStateException.class, () -> identity.updatePostcode("CB1 8GB"));
    }

    @Test
    void newIdentityShouldHaveEmptyStatusHistory() {
        assertTrue(identity.getStatusHistory().isEmpty());
    }

    @Test
    void suspendShouldRecordStatusHistory() {
        identity.suspend();
        assertFalse(identity.getStatusHistory().isEmpty());
    }

    @Test
    void shouldDetectSuspensionDuringPeriod() {
        identity.suspend();
        assertTrue(identity.wasSuspendedBetween(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)));
    }

    @Test
    void shouldNotDetectSuspensionOutsidePeriod() {
        identity.suspend();
        assertFalse(identity.wasSuspendedBetween(LocalDate.now().plusDays(10), LocalDate.now().plusDays(20)));
    }

    @Test
    void shouldCountSuspensions() {
        assertEquals(0, identity.getSuspensionCount());
        identity.suspend();
        assertEquals(1, identity.getSuspensionCount());
        identity.activate();
        identity.suspend();
        assertEquals(2, identity.getSuspensionCount());
    }
}
