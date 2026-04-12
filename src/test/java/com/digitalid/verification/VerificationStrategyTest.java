package com.digitalid.verification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.digitalid.domain.DigitalID;

class VerificationStrategyTest {

    private DigitalID identity;

    @BeforeEach
    void setUp() {
        identity = new DigitalID("AB-00-01-C", "Jane", "Smith", "Female",
                LocalDate.of(1995, 6, 15), "British", "10 High Street", "E1 7AA");
    }

    @Test
    void employerShouldReturnValidForActiveIdentity() {
        VerificationResult result = new EmployerVerificationStrategy().verify(identity);
        assertTrue(result.isValid());
    }

    @Test
    void employerShouldReturnInvalidForSuspendedIdentity() {
        identity.suspend();
        VerificationResult result = new EmployerVerificationStrategy().verify(identity);
        assertFalse(result.isValid());
    }
    
    @Test
    void bankShouldReturnValidForActiveIdentity() {
        VerificationResult result = new BankVerificationStrategy().verify(identity);
        assertTrue(result.isValid());
    }

    @Test
    void bankShouldReturnInvalidForRevokedIdentity() {
        identity.revoke();
        VerificationResult result = new BankVerificationStrategy().verify(identity);
        assertFalse(result.isValid());
    }

    @Test
    void taxAuthorityShouldReturnValidForActiveIdentity() {
        VerificationResult result = new TaxAuthorityVerificationStrategy().verify(identity);
        assertTrue(result.isValid());
    }

    @Test
    void taxAuthorityShouldReturnSuspensionMessageForSuspendedIdentity() {
        identity.suspend();
        VerificationResult result = new TaxAuthorityVerificationStrategy().verify(identity);
        assertFalse(result.isValid());
        assertTrue(result.getReason().toLowerCase().contains("suspended"));
    }

    @Test
    void drivingAuthorityShouldReturnValidForActiveUnrestrictedIdentity() {
        VerificationResult result = new DrivingAuthorityVerificationStrategy().verify(identity);
        assertTrue(result.isValid());
    }

    @Test
    void drivingAuthorityShouldReturnInvalidForRestrictedIdentity() {
        identity.setRestriction(true);
        VerificationResult result = new DrivingAuthorityVerificationStrategy().verify(identity);
        assertFalse(result.isValid());
        assertTrue(result.getReason().toLowerCase().contains("restriction"));
    }

    @Test
    void taxAuthorityShouldReturnValidWhenNoSuspensionInPeriod() {
        TaxAuthorityVerificationStrategy strategy = new TaxAuthorityVerificationStrategy();
        VerificationResult result = strategy.verify(identity, LocalDate.now().minusDays(30), LocalDate.now());
        assertTrue(result.isValid());
    }

    @Test
    void taxAuthorityShouldReturnInvalidWhenSuspendedDuringPeriod() {
        identity.suspend();
        identity.activate();
        TaxAuthorityVerificationStrategy strategy = new TaxAuthorityVerificationStrategy();
        VerificationResult result = strategy.verify(identity, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertFalse(result.isValid());
        assertTrue(result.getReason().toLowerCase().contains("suspended"));
    }

    @Test
    void welfareShouldReturnValidForActiveIdentityWithNoSuspensions() {
        VerificationResult result = new WelfareAuthorityVerificationStrategy().verify(identity);
        assertTrue(result.isValid());
    }

    @Test
    void welfareShouldReturnValidForIdentityWithOneSuspension() {
        identity.suspend();
        identity.activate();
        VerificationResult result = new WelfareAuthorityVerificationStrategy().verify(identity);
        assertTrue(result.isValid());
    }

    @Test
    void welfareShouldReturnInvalidForIdentityWithExcessiveSuspensions() {
        identity.suspend();
        identity.activate();
        identity.suspend();
        identity.activate();
        identity.suspend();
        identity.activate();
        VerificationResult result = new WelfareAuthorityVerificationStrategy().verify(identity);
        assertFalse(result.isValid());
        assertTrue(result.getReason().toLowerCase().contains("suspension"));
    }

    @Test
    void welfareShouldReturnInvalidForInactiveIdentity() {
        identity.revoke();
        VerificationResult result = new WelfareAuthorityVerificationStrategy().verify(identity);
        assertFalse(result.isValid());
    }

}

