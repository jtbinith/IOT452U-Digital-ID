package com.digitalid.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.digitalid.audit.AuditService;
import com.digitalid.domain.AttributeRule;
import com.digitalid.domain.DigitalID;
import com.digitalid.domain.IdentityAttribute;
import com.digitalid.domain.IdentityStatus;
import com.digitalid.domain.OrganisationType;
import com.digitalid.domain.StatusTransitionValidator;
import com.digitalid.exception.IdentityNotFoundException;
import com.digitalid.exception.UnauthorisedAccessException;
import com.digitalid.repository.IdentityRepository;
import com.digitalid.util.IdGenerator;

// identity management: creation, updates, status changes, restrcitions, lookups

public class IdentityService {

    private final IdentityRepository repository;
    private final AuditService auditService;
    private final AuthorisationService authService;
    private final StatusTransitionValidator transitionValidator;
    private final AttributeRule attributeRule;
    private final IdGenerator idGenerator;

    public IdentityService(IdentityRepository repository, AuditService auditService,
                           AuthorisationService authService, StatusTransitionValidator transitionValidator,
                           AttributeRule attributeRule, IdGenerator idGenerator) {
        this.repository = repository;
        this.auditService = auditService;
        this.authService = authService;
        this.transitionValidator = transitionValidator;
        this.attributeRule = attributeRule;
        this.idGenerator = idGenerator;
    }

    public DigitalID createIdentity(String name, String gender, String dob, String nationality, OrganisationType org) {
        if (!authService.canModifyIdentity(org)) {
            throw new UnauthorisedAccessException(org, "create identity");
        }
        String id = idGenerator.generateId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DigitalID identity = new DigitalID(id, name, gender, LocalDate.parse(dob, formatter), nationality);
        repository.save(identity);
        auditService.recordEvent("IDENTITY_CREATED", id, org, "SUCCESS");
        return identity;
    }

    public void updateNationality(String id, String nationality, OrganisationType org) {
        if (!authService.canModifyIdentity(org)) {
            throw new UnauthorisedAccessException(org, "update identity");
        }
        attributeRule.validateMutable(IdentityAttribute.NATIONALITY);
        DigitalID identity = findIdentityOrThrow(id);
        identity.updateNationality(nationality);
        repository.save(identity);
        auditService.recordEvent("IDENTITY_UPDATED", id, org, "NATIONALITY changed to " + nationality);
    }

    public void updateAddress(String id, String address, OrganisationType org) {
        if (!authService.canModifyIdentity(org)) {
            throw new UnauthorisedAccessException(org, "update identity");
        }
        attributeRule.validateMutable(IdentityAttribute.ADDRESS);
        DigitalID identity = findIdentityOrThrow(id);
        identity.updateAddress(address);
        repository.save(identity);
        auditService.recordEvent("IDENTITY_UPDATED", id, org, "ADDRESS changed to " + address);
    }

    public void suspendIdentity(String id, OrganisationType org) {
        if (!authService.canModifyIdentity(org)) {
            throw new UnauthorisedAccessException(org, "change identity status");
        }
        DigitalID identity = findIdentityOrThrow(id);
        transitionValidator.validate(id, identity.getStatus(), IdentityStatus.SUSPENDED);
        identity.suspend();
        repository.save(identity);
        auditService.recordEvent("STATUS_CHANGED", id, org, "ACTIVE → SUSPENDED");
    }

    public void activateIdentity(String id, OrganisationType org) {
        if (!authService.canModifyIdentity(org)) {
            throw new UnauthorisedAccessException(org, "change identity status");
        }
        DigitalID identity = findIdentityOrThrow(id);
        transitionValidator.validate(id, identity.getStatus(), IdentityStatus.ACTIVE);
        identity.activate();
        repository.save(identity);
        auditService.recordEvent("STATUS_CHANGED", id, org, "SUSPENDED → ACTIVE");
    }

    public void revokeIdentity(String id, OrganisationType org) {
        if (!authService.canModifyIdentity(org)) {
            throw new UnauthorisedAccessException(org, "change identity status");
        }
        DigitalID identity = findIdentityOrThrow(id);
        transitionValidator.validate(id, identity.getStatus(), IdentityStatus.REVOKED);
        identity.revoke();
        repository.save(identity);
        auditService.recordEvent("STATUS_CHANGED", id, org, identity.getStatus() + " → REVOKED");
    }

    public void setRestriction(String id, boolean restricted, OrganisationType org) {
        if (!authService.canModifyIdentity(org)) {
            throw new UnauthorisedAccessException(org, "modify identity restriction");
        }
        DigitalID identity = findIdentityOrThrow(id);
        identity.setRestriction(restricted);
        repository.save(identity);
        String action = restricted ? "RESTRICTION APPLIED" : "RESTRICTION REMOVED";
        auditService.recordEvent("IDENTITY_UPDATED", id, org, action);
    }

    public DigitalID findIdentity(String id) {
        return findIdentityOrThrow(id);
    }

    public List<DigitalID> findIdentity(String name, LocalDate dob) {
        return repository.findAll().stream()
            .filter(identity -> identity.getName().equalsIgnoreCase(name)
                    && identity.getDateOfBirth().equals(dob))
            .collect(Collectors.toList());
    }

    public List<DigitalID> getAllIdentities() {
        return repository.findAll();
    }

    private DigitalID findIdentityOrThrow(String id) {
        DigitalID identity = repository.findById(id);
        if (identity == null) {
            throw new IdentityNotFoundException(id);
        }
        return identity;
    }
}
