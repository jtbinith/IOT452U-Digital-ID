package com.digitalid.service;

import com.digitalid.domain.OrganisationType;

public class AuthorisationService {
    public boolean canModifyIdentity(OrganisationType org) {
        return org == OrganisationType.CENTRAL_AUTHORITY;
    }
}
