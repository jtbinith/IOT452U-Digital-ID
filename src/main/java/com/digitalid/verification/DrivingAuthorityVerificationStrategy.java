package com.digitalid.verification;

import com.digitalid.domain.DigitalID;

public class DrivingAuthorityVerificationStrategy implements VerificationStrategy {

    @Override
    public VerificationResult verify(DigitalID identity) {
        if (!identity.isActive()) {
            return new VerificationResult(false, "Identity is not active");
        }
        if (identity.isRestricted()) {
            return new VerificationResult(false, "Identity is subject to a restriction");
        }
        return new VerificationResult(true, "Identity is active and eligible for licensing");
    }
}