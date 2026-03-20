package com.digitalid.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.digitalid.audit.AuditService;
import com.digitalid.domain.AttributeRule;
import com.digitalid.domain.DigitalID;
import com.digitalid.domain.OrganisationType;
import com.digitalid.domain.StatusTransitionValidator;
import com.digitalid.exception.IdentityNotFoundException;
import com.digitalid.exception.UnauthorisedAccessException;
import com.digitalid.repository.InMemoryIdentityRepository;
import com.digitalid.util.IdGenerator;

import static org.junit.jupiter.api.Assertions.*;

class IdentityServiceTest {

    private IdentityService service;

    @BeforeEach
    void setUp() {
        service = new IdentityService(
                new InMemoryIdentityRepository(),
                new AuditService(),
                new AuthorisationService(),
                new StatusTransitionValidator(),
                new AttributeRule(),
                new IdGenerator()
        );
    }

    @Test
    void centralAuthorityShouldCreateIdentity() {
        DigitalID identity = service.createIdentity("Jane", "Smith", "Female",
                "15-06-1995", "British", "10 High Street", "E1 7AA", OrganisationType.CENTRAL_AUTHORITY);

        assertNotNull(identity);
        assertEquals("Jane Smith", identity.getFullName());
        assertEquals("British", identity.getNationality());
        assertTrue(identity.isActive());
    }

    @Test
    void consumingOrgShouldNotCreateIdentity() {
        assertThrows(UnauthorisedAccessException.class,
                () -> service.createIdentity("Jane", "Smith", "Female",
                        "15-06-1995", "British", "", "", OrganisationType.BANK));
    }

    @Test
    void shouldFindIdentityById() {
        DigitalID created = service.createIdentity("Jane", "Smith", "Female",
                "15-06-1995", "British", "", "", OrganisationType.CENTRAL_AUTHORITY);

        DigitalID found = service.findIdentity(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void shouldThrowWhenIdentityNotFound() {
        assertThrows(IdentityNotFoundException.class,
                () -> service.findIdentity("XX-99-99-Z"));
    }

    @Test
    void shouldUpdateNationality() {
        DigitalID identity = service.createIdentity("Jane", "Smith", "Female",
                "15-06-1995", "British", "", "", OrganisationType.CENTRAL_AUTHORITY);

        service.updateNationality(identity.getId(), "French", OrganisationType.CENTRAL_AUTHORITY);

        DigitalID updated = service.findIdentity(identity.getId());
        assertEquals("French", updated.getNationality());
    }

    @Test
    void consumingOrgShouldNotUpdateNationality() {
        DigitalID identity = service.createIdentity("Jane", "Smith", "Female",
                "15-06-1995", "British", "", "", OrganisationType.CENTRAL_AUTHORITY);

        assertThrows(UnauthorisedAccessException.class,
                () -> service.updateNationality(identity.getId(), "French", OrganisationType.EMPLOYER));
    }

    @Test
    void shouldSuspendAndReactivateIdentity() {
        DigitalID identity = service.createIdentity("Jane", "Smith", "Female",
                "15-06-1995", "British", "", "", OrganisationType.CENTRAL_AUTHORITY);
        String id = identity.getId();

        service.suspendIdentity(id, OrganisationType.CENTRAL_AUTHORITY);
        assertTrue(service.findIdentity(id).isSuspended());

        service.activateIdentity(id, OrganisationType.CENTRAL_AUTHORITY);
        assertTrue(service.findIdentity(id).isActive());
    }

    @Test
    void shouldRevokeIdentity() {
        DigitalID identity = service.createIdentity("Jane", "Smith", "Female",
                "15-06-1995", "British", "", "", OrganisationType.CENTRAL_AUTHORITY);

        service.revokeIdentity(identity.getId(), OrganisationType.CENTRAL_AUTHORITY);
        assertTrue(service.findIdentity(identity.getId()).isRevoked());
    }
}
