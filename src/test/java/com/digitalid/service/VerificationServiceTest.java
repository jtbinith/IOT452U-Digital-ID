package com.digitalid.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.digitalid.audit.AuditService;
import com.digitalid.domain.DigitalID;
import com.digitalid.domain.OrganisationType;
import com.digitalid.repository.IdentityRepository;
import com.digitalid.repository.InMemoryIdentityRepository;
import com.digitalid.verification.VerificationResult;

class VerificationServiceTest {
    
    private VerificationService verificationService;
    private IdentityRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryIdentityRepository();
        verificationService = new VerificationService(repository, new AuditService());
    }

    @Test
    void shouldReturnValidWhenEmployerVerifiesActiveIdentity() {
        DigitalID identity = new DigitalID("AB-00-01-C", "Jane", "Smith", "Female",
            LocalDate.of(1995, 6, 15), "British", "10 High Street", "E1 7AA");
        repository.save(identity);

        VerificationResult result = verificationService.verifyIdentity("AB-00-01-C", OrganisationType.EMPLOYER);
        assertTrue(result.isValid());

    }

    @Test
    void differentOrgsShouldGetDifferentResultsForRestrictedIdentity() {
        DigitalID identity = new DigitalID("AB-00-01-C", "Jane", "Smith", "Female",
                LocalDate.of(1995, 6, 15), "British", "10 High Street", "E1 7AA");
        identity.setRestriction(true);
        repository.save(identity);

        VerificationResult employerResult = verificationService.verifyIdentity("AB-00-01-C", OrganisationType.EMPLOYER);
        VerificationResult drivingResult = verificationService.verifyIdentity("AB-00-01-C", OrganisationType.DRIVING_AUTHORITY);

        assertTrue(employerResult.isValid());
        assertFalse(drivingResult.isValid());
    }

    @Test
    void shouldThrowWhenVerifyingNonExistentIdentity() {
        assertThrows(com.digitalid.exception.IdentityNotFoundException.class, 
            () -> verificationService.verifyIdentity("XX-99-99-Z", OrganisationType.EMPLOYER));
    }

}
