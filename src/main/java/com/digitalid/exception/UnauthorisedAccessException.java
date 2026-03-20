package com.digitalid.exception;

import com.digitalid.domain.OrganisationType;

public class UnauthorisedAccessException extends DigitalIdException {

    private final OrganisationType organisation;
    private final String attemptedAction;

    public UnauthorisedAccessException(OrganisationType organisation, String attemptedAction) {
        super(organisation + " is not authorised to " + attemptedAction, null);
        this.organisation = organisation;
        this.attemptedAction = attemptedAction;
    }

    public OrganisationType getOrganisation() {
        return organisation;
    }

    public String getAttemptedAction() {
        return attemptedAction;
    }
}