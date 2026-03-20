package com.digitalid.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.digitalid.domain.OrganisationType;

public class AuditService {

    private final List<AuditLogEntry> auditLogs = new ArrayList<>();

    public void recordEvent(String operation, String identityId, OrganisationType org, String result) {
        auditLogs.add(new AuditLogEntry(operation, identityId, org, result));
    }

    public List<AuditLogEntry> getAuditLogs() {
        return new ArrayList<>(auditLogs);
    }

    public List<AuditLogEntry> getLogsForIdentity(String identityId) {
        return auditLogs.stream()
            .filter(entry -> identityId.equals(entry.getIdentityId()))
            .collect(Collectors.toList());
    }
}