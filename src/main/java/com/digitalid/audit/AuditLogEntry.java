package com.digitalid.audit;

import java.time.LocalDateTime;
import com.digitalid.domain.OrganisationType;;

public class AuditLogEntry {
    
    private final LocalDateTime timestamp;
    private final String operation;
    private final String identityId;
    private final OrganisationType organisation;
    private final String result;

    public AuditLogEntry(String operation, String identityId, OrganisationType organisation, String result) {
        this.timestamp = LocalDateTime.now();
        this.operation = operation;
        this.identityId = identityId;
        this.organisation = organisation;
        this.result = result;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getOperation() { return operation; }
    public String getIdentityId() { return identityId; }
    public OrganisationType getOrganisation() { return organisation; }
    public String getResult() { return result; }
}
