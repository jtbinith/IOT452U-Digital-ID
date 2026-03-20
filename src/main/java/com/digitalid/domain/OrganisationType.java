package com.digitalid.domain;

// represents the different types of organisation that interact with the platform
// CA manages identities
// rest can only verify

public enum OrganisationType {
    CENTRAL_AUTHORITY,
    TAX_AUTHORITY,
    DRIVING_AUTHORITY,
    BANK,
    EMPLOYER
}
