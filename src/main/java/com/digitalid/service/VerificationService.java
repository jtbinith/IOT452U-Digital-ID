package com.digitalid.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import com.digitalid.domain.DigitalID;
import com.digitalid.domain.OrganisationType;
import com.digitalid.repository.IdentityRepository;
import com.digitalid.audit.AuditEventType;
import com.digitalid.audit.AuditService;
import com.digitalid.verification.*;
import com.digitalid.exception.IdentityNotFoundException;

public class VerificationService {

    private final IdentityRepository repository;
    private final AuditService auditService;
    private final Map<OrganisationType, VerificationStrategy> strategies;

    public VerificationService(IdentityRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
        this.strategies = new HashMap<>();
        strategies.put(OrganisationType.EMPLOYER, new EmployerVerificationStrategy());
        strategies.put(OrganisationType.BANK, new BankVerificationStrategy());
        strategies.put(OrganisationType.TAX_AUTHORITY, new TaxAuthorityVerificationStrategy());
        strategies.put(OrganisationType.DRIVING_AUTHORITY, new DrivingAuthorityVerificationStrategy());
        strategies.put(OrganisationType.WELFARE_AUTHORITY, new WelfareAuthorityVerificationStrategy());
    }

    public VerificationResult verifyIdentity(String id, OrganisationType org) {
        DigitalID identity = findIdentityOrThrow(id);

        VerificationStrategy strategy = strategies.get(org);
        VerificationResult result = strategy.verify(identity);

        auditService.recordEvent(AuditEventType.VERIFICATION_REQUESTED, id, org,
            result.isValid() ? "VALID" : "INVALID - " + result.getReason());

        return result;
    }

    public VerificationResult verifyIdentityWithPeriod(String id, OrganisationType org, LocalDate from, LocalDate to) {
        DigitalID identity = findIdentityOrThrow(id);

        TaxAuthorityVerificationStrategy strategy = (TaxAuthorityVerificationStrategy) strategies.get(org);
        VerificationResult result = strategy.verify(identity, from, to);

        auditService.recordEvent(AuditEventType.VERIFICATION_REQUESTED, id, org,
            result.isValid() ? "VALID" : "INVALID - " + result.getReason());

        return result;
    }

    private DigitalID findIdentityOrThrow(String id) {
        DigitalID identity = repository.findById(id);
        if (identity == null) {
            throw new IdentityNotFoundException(id);
        }
        return identity;
    }
}